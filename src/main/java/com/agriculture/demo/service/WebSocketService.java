package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.enums.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息推送服务
 * 负责向客户端推送各种实时数据
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 推送传感器数据
     */
    public void pushSensorData(Long plotId, Map<String, Object> data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "SENSOR_DATA");
            message.put("timestamp",
                    data.get("temperature") != null ? java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER) : null);
            message.put("data", data);

            messagingTemplate.convertAndSend("/topic/realtime/" + plotId,
                    objectMapper.writeValueAsString(message));

            logger.debug("推送传感器数据成功 - 地块: {}", plotId);
        } catch (Exception e) {
            logger.error("推送传感器数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送设备状态变化
     */
    public void pushDeviceStatus(Device device) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("deviceId", device.getId());
            data.put("deviceCode", device.getDeviceCode());
            data.put("deviceName", device.getDeviceName());
            data.put("status", device.getStatus());
            data.put("lastHeartbeat",
                    device.getLastHeartbeat() != null ? device.getLastHeartbeat().format(DATE_TIME_FORMATTER) : null);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "DEVICE_STATUS");
            message.put("timestamp", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));
            message.put("data", data);

            messagingTemplate.convertAndSend("/topic/realtime/" + device.getPlotId(),
                    objectMapper.writeValueAsString(message));

            logger.info("推送设备状态成功 - 设备: {}, 状态: {}", device.getDeviceCode(), device.getStatus());
        } catch (Exception e) {
            logger.error("推送设备状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送灌溉状态变化
     */
    public void pushIrrigationStatus(Plot plot) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("plotId", plot.getId());
            data.put("irrigationStatus", plot.getCurrentIrrigationStatus());
            data.put("irrigationStatusName",
                    IrrigationStatusEnum.OPEN.getCode().equals(plot.getCurrentIrrigationStatus())
                            ? "已开启"
                            : "已关闭");
            data.put("updateTime", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));

            Map<String, Object> message = new HashMap<>();
            message.put("type", "IRRIGATION_STATUS");
            message.put("timestamp", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));
            message.put("data", data);

            messagingTemplate.convertAndSend("/topic/realtime/" + plot.getId(),
                    objectMapper.writeValueAsString(message));

            logger.info("推送灌溉状态成功 - 地块: {}, 状态: {}", plot.getPlotName(), plot.getCurrentIrrigationStatus());
        } catch (Exception e) {
            logger.error("推送灌溉状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送灌溉模式变化
     */
    public void pushIrrigationMode(Plot plot) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("plotId", plot.getId());
            data.put("irrigationMode", plot.getIrrigationMode());
            data.put("irrigationModeName", IrrigationModeEnum.AUTO.getCode().equals(plot.getIrrigationMode())
                    ? "自动模式"
                    : "手动模式");
            data.put("updateTime", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));

            Map<String, Object> message = new HashMap<>();
            message.put("type", "IRRIGATION_MODE");
            message.put("timestamp", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));
            message.put("data", data);

            messagingTemplate.convertAndSend("/topic/realtime/" + plot.getId(),
                    objectMapper.writeValueAsString(message));

            logger.info("推送灌溉模式成功 - 地块: {}, 模式: {}", plot.getPlotName(), plot.getIrrigationMode());
        } catch (Exception e) {
            logger.error("推送灌溉模式失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送自动灌溉触发消息
     */
    public void pushAutoIrrigationTrigger(Long plotId, Map<String, Object> data) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "AUTO_IRRIGATION_TRIGGER");
            message.put("timestamp", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));
            message.put("data", data);

            messagingTemplate.convertAndSend("/topic/realtime/" + plotId,
                    objectMapper.writeValueAsString(message));

            logger.info("推送自动灌溉触发消息成功 - 地块: {}", plotId);
        } catch (Exception e) {
            logger.error("推送自动灌溉触发消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送告警通知
     */
    public void pushAlert(AlertLog alertLog) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("alertId", alertLog.getId());
            data.put("plotId", alertLog.getPlotId());
            data.put("deviceId", alertLog.getDeviceId());
            data.put("alertType", alertLog.getAlertType());
            data.put("alertTypeName", AlertTypeEnum.getByCode(alertLog.getAlertType()) != null
                    ? AlertTypeEnum.getByCode(alertLog.getAlertType()).getName()
                    : "");
            data.put("triggerValue", alertLog.getTriggerValue());
            data.put("thresholdValue", alertLog.getThresholdValue());
            data.put("alertTime", alertLog.getCreateTime() != null
                    ? alertLog.getCreateTime().format(DATE_TIME_FORMATTER)
                    : null);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "NEW_ALERT");
            message.put("timestamp", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));
            message.put("data", data);

            String messageJson = objectMapper.writeValueAsString(message);

            // 推送到全局告警主题（接收所有告警）
            messagingTemplate.convertAndSend("/topic/alerts", messageJson);

            // 推送到按地块订阅的告警主题（只接收该地块的告警）
            if (alertLog.getPlotId() != null) {
                messagingTemplate.convertAndSend("/topic/alerts/plot/" + alertLog.getPlotId(), messageJson);
            }

            // 同时推送到对应地块的实时数据主题
            if (alertLog.getPlotId() != null) {
                messagingTemplate.convertAndSend("/topic/realtime/" + alertLog.getPlotId(), messageJson);
            }

            logger.info("推送告警通知成功 - 告警: {}, 地块: {}", alertLog.getAlertType(), alertLog.getPlotId());
        } catch (Exception e) {
            logger.error("推送告警通知失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送告警状态更新
     */
    public void pushAlertStatusUpdate(AlertLog alertLog) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("alertId", alertLog.getId());
            data.put("status", alertLog.getStatus());
            data.put("statusName", AlertStatusEnum.getByCode(alertLog.getStatus()) != null
                    ? AlertStatusEnum.getByCode(alertLog.getStatus()).getName()
                    : "");
            data.put("updateTime", alertLog.getUpdateTime() != null
                    ? alertLog.getUpdateTime().format(DATE_TIME_FORMATTER)
                    : null);

            Map<String, Object> message = new HashMap<>();
            message.put("type", "ALERT_STATUS_UPDATE");
            message.put("timestamp", java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER));
            message.put("data", data);

            String messageJson = objectMapper.writeValueAsString(message);

            // 推送到全局告警主题
            messagingTemplate.convertAndSend("/topic/alerts", messageJson);

            // 推送到按地块订阅的告警主题
            if (alertLog.getPlotId() != null) {
                messagingTemplate.convertAndSend("/topic/alerts/plot/" + alertLog.getPlotId(), messageJson);
            }

            // 同时推送到对应地块的实时数据主题
            if (alertLog.getPlotId() != null) {
                messagingTemplate.convertAndSend("/topic/realtime/" + alertLog.getPlotId(), messageJson);
            }

            logger.info("推送告警状态更新成功 - 告警: {}, 状态: {}", alertLog.getId(), alertLog.getStatus());
        } catch (Exception e) {
            logger.error("推送告警状态更新失败: {}", e.getMessage(), e);
        }
    }
}
