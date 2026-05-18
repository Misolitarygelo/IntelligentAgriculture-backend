package com.agriculture.demo.service;

import com.agriculture.demo.entity.*;
import com.agriculture.demo.enums.*;
import com.agriculture.demo.mapper.*;
import com.agriculture.demo.mqtt.MqttPublisher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 自动灌溉服务
 * 负责根据土壤湿度自动控制灌溉
 */
@Service
public class AutoIrrigationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoIrrigationService.class);

    @Autowired
    private PlotMapper plotMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private ThresholdConfigMapper thresholdConfigMapper;

    @Autowired
    private IrrigationLogMapper irrigationLogMapper;

    @Autowired
    private MqttPublisher mqttPublisher;

    @Autowired
    private WebSocketService webSocketService;

    /**
     * 检查并执行自动灌溉
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAutoIrrigation(Long plotId, BigDecimal humidity) {
        // 1. 查询地块信息
        Plot plot = plotMapper.selectById(plotId);
        if (plot == null) {
            return;
        }
        
        // 2. 检查是否为自动模式
        if (!IrrigationModeEnum.AUTO.getCode().equals(plot.getIrrigationMode())) {
            return;
        }
        
        logger.info("自动灌溉模式 - 地块: {}, 当前湿度: {}, 当前灌溉状态: {}", 
            plot.getPlotName(), humidity, plot.getCurrentIrrigationStatus());
        
        // 3. 获取阈值配置
        Map<String, BigDecimal> thresholds = getThresholds(plotId);
        
        // 4. 判断是否需要开启灌溉
        if ("CLOSED".equals(plot.getCurrentIrrigationStatus())) {
            if (thresholds.containsKey("MOISTURE_LOW") && 
                humidity.compareTo(thresholds.get("MOISTURE_LOW")) < 0) {
                // 需要开启灌溉
                logger.info("自动开启灌溉 - 地块: {}, 湿度过低: {}", plot.getPlotName(), humidity);
                autoControlIrrigation(plot, "on", "湿度过低自动开启", humidity, thresholds.get("MOISTURE_LOW"));
            }
        }
        
        // 5. 判断是否需要关闭灌溉
        else if ("OPEN".equals(plot.getCurrentIrrigationStatus())) {
            if (thresholds.containsKey("MOISTURE_HIGH") && 
                humidity.compareTo(thresholds.get("MOISTURE_HIGH")) > 0) {
                // 需要关闭灌溉
                logger.info("自动关闭灌溉 - 地块: {}, 湿度过高: {}", plot.getPlotName(), humidity);
                autoControlIrrigation(plot, "off", "湿度过高自动关闭", humidity, thresholds.get("MOISTURE_HIGH"));
            }
        }
    }

    /**
     * 自动控制灌溉
     */
    private void autoControlIrrigation(Plot plot, String operation, String remark, BigDecimal humidity, BigDecimal threshold) {
        // 1. 查询该地块的灌溉设备
        Device irrigationDevice = deviceMapper.selectOne(
            new LambdaQueryWrapper<Device>()
                .eq(Device::getPlotId, plot.getId())
                .eq(Device::getDeviceType, DeviceTypeEnum.IRRIGATION.getCode())
        );
        
        if (irrigationDevice == null) {
            logger.warn("地块 {} 没有找到灌溉设备", plot.getPlotName());
            return;
        }
        
        // 2. 生成唯一msgId
        String msgId = UUID.randomUUID().toString();
        
        // 3. 创建灌溉记录
        IrrigationLog irrigationLog = new IrrigationLog();
        irrigationLog.setPlotId(plot.getId());
        irrigationLog.setOperation("on".equals(operation) ? "OPEN" : "CLOSE");
        irrigationLog.setOperationSource(OperationSourceEnum.AUTO.getCode());
        irrigationLog.setMsgId(msgId);
        irrigationLog.setRemark(remark);
        irrigationLog.setCreateTime(LocalDateTime.now());
        irrigationLogMapper.insert(irrigationLog);
        
        // 4. 发送MQTT控制指令
        mqttPublisher.publishIrrigationControl(msgId, operation, irrigationDevice.getDeviceCode());
        
        // 5. WebSocket推送自动灌溉触发消息
        Map<String, Object> data = new HashMap<>();
        data.put("plotId", plot.getId());
        data.put("triggerType", "on".equals(operation) ? "LOW_MOISTURE" : "HIGH_MOISTURE");
        data.put("currentMoisture", humidity);
        data.put("thresholdMoisture", threshold);
        data.put("action", "on".equals(operation) ? "OPEN" : "CLOSE");
        webSocketService.pushAutoIrrigationTrigger(plot.getId(), data);
        
        logger.info("自动灌溉控制指令已发送 - 地块: {}, 操作: {}", plot.getPlotName(), operation);
    }

    /**
     * 获取阈值配置
     */
    private Map<String, BigDecimal> getThresholds(Long plotId) {
        Map<String, BigDecimal> result = new HashMap<>();
        thresholdConfigMapper.selectList(
            new LambdaQueryWrapper<ThresholdConfig>()
                .eq(ThresholdConfig::getPlotId, plotId)
        ).forEach(config -> {
            result.put(config.getConfigType(), config.getThresholdValue());
        });
        return result;
    }
}
