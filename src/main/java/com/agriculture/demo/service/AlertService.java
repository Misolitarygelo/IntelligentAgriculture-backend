package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.enums.*;
import com.agriculture.demo.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警服务类
 */
@Service
public class AlertService {

    @Autowired
    private AlertLogMapper alertLogMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private PlotMapper plotMapper;

    /**
     * 获取告警列表（分页）
     */
    public Page<AlertLog> getAlertList(int page, int size, Long plotId, String status, String alertType) {
        Page<AlertLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AlertLog> wrapper = new LambdaQueryWrapper<>();

        if (plotId != null) {
            wrapper.eq(AlertLog::getPlotId, plotId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AlertLog::getStatus, status);
        }
        if (alertType != null && !alertType.isEmpty()) {
            wrapper.eq(AlertLog::getAlertType, alertType);
        }

        wrapper.orderByDesc(AlertLog::getCreateTime);
        Page<AlertLog> alertPage = alertLogMapper.selectPage(pageParam, wrapper);

        // 填充设备编码和地块名称
        fillAlertRelations(alertPage.getRecords());

        return alertPage;
    }

    /**
     * 获取最近告警
     */
    public List<AlertLog> getRecentAlerts(int limit) {
        List<AlertLog> alerts = alertLogMapper.selectList(
                new LambdaQueryWrapper<AlertLog>()
                        .orderByDesc(AlertLog::getCreateTime)
                        .last("LIMIT " + limit));

        // 填充设备编码和地块名称
        fillAlertRelations(alerts);

        return alerts;
    }

    /**
     * 处理告警
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean processAlert(Long alertId, String handleType) {
        AlertLog alertLog = alertLogMapper.selectById(alertId);
        if (alertLog == null) {
            return false;
        }

        // 根据处理类型设置状态
        if ("PROCESSING".equals(handleType)) {
            alertLog.setStatus(AlertStatusEnum.PROCESSING.getCode());
        } else if ("DONE".equals(handleType)) {
            alertLog.setStatus(AlertStatusEnum.DONE.getCode());
            alertLog.setHandleTime(LocalDateTime.now());
        } else if ("IGNORED".equals(handleType)) {
            alertLog.setStatus(AlertStatusEnum.IGNORED.getCode());
        }

        alertLog.setHandleType(handleType);
        alertLog.setUpdateTime(LocalDateTime.now());

        int rows = alertLogMapper.updateById(alertLog);

        if (rows > 0) {
            webSocketService.pushAlertStatusUpdate(alertLog);
        }

        return rows > 0;
    }

    /**
     * 获取告警详情
     */
    public AlertLog getAlertById(Long id) {
        AlertLog alertLog = alertLogMapper.selectById(id);
        if (alertLog != null) {
            fillAlertRelation(alertLog);
        }
        return alertLog;
    }

    /**
     * 填充告警列表的设备编码和地块名称
     */
    private void fillAlertRelations(List<AlertLog> alerts) {
        for (AlertLog alert : alerts) {
            fillAlertRelation(alert);
        }
    }

    /**
     * 填充单个告警的设备编码和地块名称
     */
    private void fillAlertRelation(AlertLog alert) {
        // 填充设备编码
        if (alert.getDeviceId() != null) {
            Device device = deviceMapper.selectById(alert.getDeviceId());
            if (device != null) {
                alert.setDeviceCode(device.getDeviceCode());
            }
        }

        // 填充地块名称
        if (alert.getPlotId() != null) {
            Plot plot = plotMapper.selectById(alert.getPlotId());
            if (plot != null) {
                alert.setPlotName(plot.getPlotName());
            }
        }
    }

    /**
     * 创建设备离线告警
     */
    @Transactional(rollbackFor = Exception.class)
    public void createDeviceOfflineAlert(Device device) {
        // 检查是否最近已有同类告警（避免重复告警）
        AlertLog recentAlert = alertLogMapper.selectOne(
                new LambdaQueryWrapper<AlertLog>()
                        .eq(AlertLog::getDeviceId, device.getId())
                        .eq(AlertLog::getAlertType, AlertTypeEnum.DEVICE_OFFLINE.getCode())
                        .eq(AlertLog::getStatus, AlertStatusEnum.PENDING.getCode())
                        .orderByDesc(AlertLog::getCreateTime)
                        .last("LIMIT 1"));

        if (recentAlert != null && recentAlert.getCreateTime().plusMinutes(10).isAfter(LocalDateTime.now())) {
            return;
        }

        // 创建新告警
        AlertLog alertLog = new AlertLog();
        alertLog.setDeviceId(device.getId());
        alertLog.setPlotId(device.getPlotId());
        alertLog.setAlertType(AlertTypeEnum.DEVICE_OFFLINE.getCode());
        alertLog.setAlertMessage(AlertTypeEnum.DEVICE_OFFLINE.getName());
        alertLog.setStatus(AlertStatusEnum.PENDING.getCode());
        alertLog.setCreateTime(LocalDateTime.now());
        alertLogMapper.insert(alertLog);

        // WebSocket推送告警
        webSocketService.pushAlert(alertLog);
    }
}