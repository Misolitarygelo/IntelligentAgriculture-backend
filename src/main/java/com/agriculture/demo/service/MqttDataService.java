package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.enums.*;
import com.agriculture.demo.mapper.*;
import com.agriculture.demo.util.SystemAlertUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MQTT数据处理服务
 * 处理从硬件设备上报的各种数据
 */
@Service
public class MqttDataService {

    private static final Logger logger = LoggerFactory.getLogger(MqttDataService.class);
    private static final String SOURCE = "MqttDataService";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private SensorDataMapper sensorDataMapper;

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private ThresholdConfigMapper thresholdConfigMapper;

    @Autowired
    private AlertLogMapper alertLogMapper;

    @Autowired
    private IrrigationLogMapper irrigationLogMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private AutoIrrigationService autoIrrigationService;

    /**
     * 处理MQTT消息
     */
    @Transactional(rollbackFor = Exception.class)
    public void processMessage(String topic, String payload) {
        try {
            if ("team6/environment_data".equals(topic)) {
                processEnvironmentData(payload);
            } else if ("team6/heart_beat".equals(topic)) {
                processHeartBeat(payload);
            } else if ("team6/irrigation".equals(topic)) {
                processIrrigationResponse(payload);
            }
        } catch (Exception e) {
            logger.error("处理MQTT消息失败: {}", e.getMessage(), e);
            // 判断是否为数据库异常
            if (isDatabaseException(e)) {
                SystemAlertUtil.logDatabaseError("MQTT数据处理失败",
                        String.format("主题: %s, 错误: %s", topic, e.getMessage()),
                        SOURCE);
            } else {
                SystemAlertUtil.logSystemError("MQTT数据处理失败",
                        String.format("主题: %s, 错误: %s", topic, e.getMessage()),
                        SOURCE);
            }
        }
    }

    /**
     * 判断是否为数据库异常
     */
    private boolean isDatabaseException(Exception e) {
        String exceptionName = e.getClass().getName();
        return exceptionName.contains("SQLException")
                || exceptionName.contains("Database")
                || exceptionName.contains("MyBatis")
                || exceptionName.contains("SQL")
                || (e.getCause() != null && isDatabaseException((Exception) e.getCause()));
    }

    /**
     * 处理环境数据
     */
    private void processEnvironmentData(String payload) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(payload);
        String deviceCode = jsonNode.get("device_code").asText();
        BigDecimal temperature = jsonNode.get("temperature").decimalValue();
        BigDecimal humidity = jsonNode.get("humidity").decimalValue();

        logger.info("处理环境数据 - 设备: {}, 温度: {}, 湿度: {}", deviceCode, temperature, humidity);

        // 1. 查询设备信息
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getDeviceCode, deviceCode));

        if (device == null) {
            logger.warn("设备不存在: {}", deviceCode);
            return;
        }

        // 2. 保存温度数据
        saveSensorData(device.getId(), DataTypeEnum.AIR_TEMP, temperature, LocalDateTime.now());

        // 3. 保存湿度数据
        saveSensorData(device.getId(), DataTypeEnum.SOIL_MOISTURE, humidity, LocalDateTime.now());

        // 4. 检查阈值并生成告警
        checkThresholdAndAlert(device, temperature, humidity);

        // 5. WebSocket推送实时数据
        Map<String, Object> data = new HashMap<>();
        data.put("plotId", device.getPlotId());
        data.put("deviceCode", deviceCode);
        data.put("temperature", temperature);
        data.put("humidity", humidity);
        webSocketService.pushSensorData(device.getPlotId(), data);

        // 6. 检查是否需要自动灌溉
        autoIrrigationService.checkAndAutoIrrigation(device.getPlotId(), humidity);
    }

    /**
     * 处理心跳数据
     */
    private void processHeartBeat(String payload) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(payload);
        String deviceCode = jsonNode.get("device_code").asText();

        logger.info("处理心跳数据 - 设备: {}", deviceCode);

        // 更新设备心跳时间和状态
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getDeviceCode, deviceCode));

        if (device != null) {
            // 如果之前是离线状态，则更新为在线
            boolean wasOffline = "OFFLINE".equals(device.getStatus());

            device.setLastHeartbeat(LocalDateTime.now());
            device.setStatus("ONLINE");
            deviceMapper.updateById(device);

            // 如果是从离线变在线，需要WebSocket推送状态变化
            if (wasOffline) {
                webSocketService.pushDeviceStatus(device);
            }
        }
    }

    /**
     * 处理灌溉控制响应
     */
    private void processIrrigationResponse(String payload) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(payload);
        String type = jsonNode.get("type").asText();

        // 只处理feed_back类型的响应
        if (!"feed_back".equals(type)) {
            return;
        }

        String msgId = jsonNode.get("msgId").asText();
        String irrigation = jsonNode.get("irrigation").asText();
        String result = jsonNode.get("result").asText();
        String deviceCode = jsonNode.get("device_code").asText();

        logger.info("处理灌溉响应 - MsgId: {}, 操作: {}, 结果: {}", msgId, irrigation, result);

        // 1. 根据msgId查找对应的灌溉记录
        IrrigationLog irrigationLog = irrigationLogMapper.selectOne(
                new LambdaQueryWrapper<IrrigationLog>()
                        .eq(IrrigationLog::getMsgId, msgId)
                        .isNull(IrrigationLog::getResult)
                        .orderByDesc(IrrigationLog::getCreateTime)
                        .last("LIMIT 1"));

        if (irrigationLog == null) {
            logger.warn("找不到对应的灌溉控制记录: {}", msgId);
            return;
        }

        // 2. 更新灌溉记录
        irrigationLog.setResult(result);
        irrigationLogMapper.updateById(irrigationLog);

        // 3. 如果成功，更新地块的灌溉状态
        if ("SUCCESS".equals(result)) {
            Device device = deviceMapper.selectOne(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getDeviceCode, deviceCode));
            if (device != null) {
                Plot plot = plotMapper.selectById(device.getPlotId());
                if (plot != null) {
                    plot.setCurrentIrrigationStatus("on".equals(irrigation) ? "OPEN" : "CLOSED");
                    plotMapper.updateById(plot);

                    // WebSocket推送灌溉状态变化
                    webSocketService.pushIrrigationStatus(plot);
                }
            }
        }
    }

    /**
     * 保存传感器数据
     */
    private void saveSensorData(Long deviceId, DataTypeEnum dataType, BigDecimal value, LocalDateTime collectTime) {
        SensorData sensorData = new SensorData();
        sensorData.setDeviceId(deviceId);
        sensorData.setDataType(dataType.getCode());
        sensorData.setValue(value);
        sensorData.setCollectTime(collectTime);
        sensorData.setCreateTime(LocalDateTime.now());
        sensorDataMapper.insert(sensorData);
    }

    /**
     * 检查阈值并生成告警
     */
    private void checkThresholdAndAlert(Device device, BigDecimal temperature, BigDecimal humidity) {
        Long plotId = device.getPlotId();

        // 查询该地块的阈值配置
        Map<String, BigDecimal> thresholds = getThresholds(plotId);

        // 检查温度过低
        if (thresholds.containsKey("TEMP_LOW") && temperature.compareTo(thresholds.get("TEMP_LOW")) < 0) {
            createAlert(device, AlertTypeEnum.TEMP_LOW, temperature, thresholds.get("TEMP_LOW"));
        }

        // 检查温度过高
        if (thresholds.containsKey("TEMP_HIGH") && temperature.compareTo(thresholds.get("TEMP_HIGH")) > 0) {
            createAlert(device, AlertTypeEnum.TEMP_HIGH, temperature, thresholds.get("TEMP_HIGH"));
        }

        // 检查湿度过低
        if (thresholds.containsKey("MOISTURE_LOW") && humidity.compareTo(thresholds.get("MOISTURE_LOW")) < 0) {
            createAlert(device, AlertTypeEnum.MOISTURE_LOW, humidity, thresholds.get("MOISTURE_LOW"));
        }

        // 检查湿度过高
        if (thresholds.containsKey("MOISTURE_HIGH") && humidity.compareTo(thresholds.get("MOISTURE_HIGH")) > 0) {
            createAlert(device, AlertTypeEnum.MOISTURE_HIGH, humidity, thresholds.get("MOISTURE_HIGH"));
        }
    }

    /**
     * 获取阈值配置
     */
    private Map<String, BigDecimal> getThresholds(Long plotId) {
        Map<String, BigDecimal> result = new HashMap<>();
        thresholdConfigMapper.selectList(
                new LambdaQueryWrapper<ThresholdConfig>()
                        .eq(ThresholdConfig::getPlotId, plotId))
                .forEach(config -> {
                    result.put(config.getConfigType(), config.getThresholdValue());
                });
        return result;
    }

    /**
     * 创建告警
     */
    private void createAlert(Device device, AlertTypeEnum alertType, BigDecimal triggerValue,
            BigDecimal thresholdValue) {
        // 检查是否最近已有同类告警（避免重复告警）
        AlertLog recentAlert = alertLogMapper.selectOne(
                new LambdaQueryWrapper<AlertLog>()
                        .eq(AlertLog::getDeviceId, device.getId())
                        .eq(AlertLog::getAlertType, alertType.getCode())
                        .eq(AlertLog::getStatus, AlertStatusEnum.PENDING.getCode())
                        .orderByDesc(AlertLog::getCreateTime)
                        .last("LIMIT 1"));

        if (recentAlert != null && recentAlert.getCreateTime().plusMinutes(10).isAfter(LocalDateTime.now())) {
            logger.info("最近已有同类告警，跳过: {}", alertType.getName());
            return;
        }

        // 创建新告警
        AlertLog alertLog = new AlertLog();
        alertLog.setDeviceId(device.getId());
        alertLog.setPlotId(device.getPlotId());
        alertLog.setAlertType(alertType.getCode());
        alertLog.setTriggerValue(triggerValue);
        alertLog.setThresholdValue(thresholdValue);
        alertLog.setAlertMessage(alertType.getName());
        alertLog.setStatus(AlertStatusEnum.PENDING.getCode());
        alertLog.setCreateTime(LocalDateTime.now());
        alertLogMapper.insert(alertLog);

        logger.info("创建告警: {}", alertType.getName());

        // WebSocket推送告警
        webSocketService.pushAlert(alertLog);
    }
}
