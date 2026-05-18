package com.agriculture.demo.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MQTT消息发布器
 * 用于向硬件设备发送控制指令
 */
@Component
public class MqttPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttPublisher.class);

    @Autowired
    private MqttClient mqttClient;

    /**
     * 发送消息
     * @param topic 主题
     * @param payload 消息内容
     * @param qos 服务质量（0、1、2）
     */
    public void publish(String topic, String payload, int qos) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            mqttClient.publish(topic, message);
            logger.info("MQTT消息发送成功 - Topic: {}, 内容: {}", topic, payload);
        } catch (MqttException e) {
            logger.error("MQTT消息发送失败 - Topic: {}, 内容: {}", topic, payload, e);
        }
    }

    /**
     * 发送消息（默认QoS为1）
     */
    public void publish(String topic, String payload) {
        publish(topic, payload, 1);
    }

    /**
     * 发送灌溉控制指令
     */
    public void publishIrrigationControl(String msgId, String irrigation, String deviceCode) {
        String payload = String.format(
            "{\"msgId\":\"%s\",\"irrigation\":\"%s\",\"device_code\":\"%s\",\"type\":\"control\"}",
            msgId, irrigation, deviceCode
        );
        publish("team6/irrigation", payload);
    }
}
