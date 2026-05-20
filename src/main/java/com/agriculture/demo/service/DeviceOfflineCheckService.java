package com.agriculture.demo.service;

import com.agriculture.demo.entity.Device;
import com.agriculture.demo.mapper.DeviceMapper;
import com.agriculture.demo.util.SystemAlertUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备离线检测服务
 * 定时检查设备心跳，检测离线设备并更新状态
 */
@Service
public class DeviceOfflineCheckService {

    private static final Logger logger = LoggerFactory.getLogger(DeviceOfflineCheckService.class);
    private static final String SOURCE = "DeviceOfflineCheckService";

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private AlertService alertService;

    /**
     * 离线超时时间（秒）
     * 设备超过此时间未发送心跳则判定为离线
     */
    private static final int OFFLINE_TIMEOUT_SECONDS = 20;

    /**
     * 定时检查设备离线状态
     * 每10秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void checkDeviceOffline() {
        try {
            // 查询所有在线设备
            List<Device> onlineDevices = deviceMapper.selectList(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getStatus, "ONLINE"));

            LocalDateTime now = LocalDateTime.now();

            for (Device device : onlineDevices) {
                // 检查心跳时间是否超时
                if (device.getLastHeartbeat() != null) {
                    long secondsSinceLastHeartbeat = java.time.Duration.between(
                            device.getLastHeartbeat(), now).getSeconds();

                    if (secondsSinceLastHeartbeat > OFFLINE_TIMEOUT_SECONDS) {
                        // 设备离线，更新状态
                        device.setStatus("OFFLINE");
                        deviceMapper.updateById(device);

                        logger.info("设备离线 - 设备: {}, 最后心跳: {}",
                                device.getDeviceCode(), device.getLastHeartbeat());

                        // WebSocket推送设备离线通知
                        webSocketService.pushDeviceStatus(device);

                        // 创建设备离线告警
                        alertService.createDeviceOfflineAlert(device);
                    }
                } else if (device.getCreateTime() != null) {
                    // 从未收到过心跳的设备，创建时间超过超时时间也判定为离线
                    long secondsSinceCreated = java.time.Duration.between(
                            device.getCreateTime(), now).getSeconds();

                    if (secondsSinceCreated > OFFLINE_TIMEOUT_SECONDS) {
                        device.setStatus("OFFLINE");
                        deviceMapper.updateById(device);

                        logger.info("设备离线（从未收到心跳）- 设备: {}", device.getDeviceCode());

                        webSocketService.pushDeviceStatus(device);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("检查设备离线状态失败: {}", e.getMessage(), e);
            // 记录定时任务执行异常告警
            SystemAlertUtil.logScheduleError("设备离线检测任务失败",
                    String.format("错误: %s", e.getMessage()),
                    SOURCE);
        }
    }
}
