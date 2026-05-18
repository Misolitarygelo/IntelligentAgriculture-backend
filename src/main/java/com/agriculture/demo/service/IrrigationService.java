package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.enums.*;
import com.agriculture.demo.mapper.*;
import com.agriculture.demo.mqtt.MqttPublisher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 灌溉控制服务类
 */
@Service
public class IrrigationService {

    private static final Logger logger = LoggerFactory.getLogger(IrrigationService.class);

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private IrrigationLogMapper irrigationLogMapper;

    @Autowired
    private MqttPublisher mqttPublisher;

    /**
     * 获取灌溉状态
     */
    public Map<String, Object> getIrrigationStatus(Long plotId) {
        Plot plot = plotMapper.selectById(plotId);
        Map<String, Object> result = new HashMap<>();

        if (plot != null) {
            result.put("plotId", plot.getId());
            result.put("irrigationStatus", plot.getCurrentIrrigationStatus());
            result.put("irrigationStatusName",
                    IrrigationStatusEnum.OPEN.getCode().equals(plot.getCurrentIrrigationStatus())
                            ? "已开启"
                            : "已关闭");
        }

        return result;
    }

    /**
     * 发送灌溉控制指令
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> controlIrrigation(Long plotId, String operation) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查询地块信息
        Plot plot = plotMapper.selectById(plotId);
        if (plot == null) {
            result.put("success", false);
            result.put("message", "地块不存在");
            return result;
        }

        // 2. 查询该地块的灌溉设备
        Device irrigationDevice = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getPlotId, plotId)
                        .eq(Device::getDeviceType, DeviceTypeEnum.IRRIGATION.getCode()));

        if (irrigationDevice == null) {
            result.put("success", false);
            result.put("message", "地块未绑定灌溉设备");
            return result;
        }

        // 3. 生成唯一msgId
        String msgId = UUID.randomUUID().toString();

        // 4. 创建灌溉记录
        IrrigationLog irrigationLog = new IrrigationLog();
        irrigationLog.setPlotId(plotId);
        irrigationLog.setOperation(operation);
        irrigationLog.setOperationSource(OperationSourceEnum.MANUAL.getCode());
        irrigationLog.setMsgId(msgId);
        irrigationLog.setRemark("手动控制");
        irrigationLog.setCreateTime(LocalDateTime.now());
        irrigationLogMapper.insert(irrigationLog);

        // 5. 发送MQTT控制指令
        String irrigationOperation = "OPEN".equals(operation) ? "on" : "off";
        mqttPublisher.publishIrrigationControl(msgId, irrigationOperation, irrigationDevice.getDeviceCode());

        logger.info("灌溉控制指令已发送 - 地块: {}, 操作: {}", plot.getPlotName(), operation);

        result.put("success", true);
        result.put("message", "控制指令已发送");
        result.put("msgId", msgId);

        return result;
    }

    /**
     * 获取灌溉记录
     */
    public Page<IrrigationLog> getIrrigationLogs(int page, int size, Long plotId) {
        Page<IrrigationLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<IrrigationLog> wrapper = new LambdaQueryWrapper<>();

        if (plotId != null) {
            wrapper.eq(IrrigationLog::getPlotId, plotId);
        }

        wrapper.orderByDesc(IrrigationLog::getCreateTime);
        return irrigationLogMapper.selectPage(pageParam, wrapper);
    }
}
