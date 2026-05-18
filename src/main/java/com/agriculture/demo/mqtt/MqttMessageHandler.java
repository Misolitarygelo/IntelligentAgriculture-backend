package com.agriculture.demo.mqtt;

import com.agriculture.demo.service.MqttDataService;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQTT消息回调处理器
 * 处理连接、断开、接收消息等事件
 */
@Component
public class MqttMessageHandler implements MqttCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttMessageHandler.class);

    @Autowired
    private MqttDataService mqttDataService;

    /**
     * 连接丢失时触发
     */
    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT连接丢失: {}", cause.getMessage(), cause);
    }

    /**
     * 收到消息时触发
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        logger.info("收到MQTT消息 - Topic: {}, 内容: {}", topic, payload);
        
        try {
            mqttDataService.processMessage(topic, payload);
        } catch (Exception e) {
            logger.error("处理MQTT消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 消息发送完成时触发
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.debug("MQTT消息发送完成，MessageId: {}", token.getMessageId());
    }
}
