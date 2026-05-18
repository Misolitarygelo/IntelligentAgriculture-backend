package com.agriculture.demo.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MQTT订阅器
 * 在应用启动时自动订阅需要的主题
 */
@Component
public class MqttSubscriber implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriber.class);

    @Autowired
    private MqttClient mqttClient;

    @Autowired
    private MqttMessageHandler mqttMessageHandler;

    @Value("${agriculture.mqtt.topic.environment-data}")
    private String envDataTopic;

    @Value("${agriculture.mqtt.topic.heart-beat}")
    private String heartBeatTopic;

    @Value("${agriculture.mqtt.topic.irrigation}")
    private String irrigationTopic;

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始订阅MQTT主题...");
        
        // 设置回调处理器
        mqttClient.setCallback(mqttMessageHandler);
        
        // 订阅环境数据主题
        subscribeTopic(envDataTopic);
        
        // 订阅心跳主题
        subscribeTopic(heartBeatTopic);
        
        // 订阅灌溉控制响应主题
        subscribeTopic(irrigationTopic);
        
        logger.info("MQTT主题订阅完成！");
    }

    /**
     * 订阅单个主题
     */
    private void subscribeTopic(String topic) {
        try {
            mqttClient.subscribe(topic, 1);
            logger.info("订阅主题成功: {}", topic);
        } catch (MqttException e) {
            logger.error("订阅主题失败: {}", topic, e);
        }
    }
}
