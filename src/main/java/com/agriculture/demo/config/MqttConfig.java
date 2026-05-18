package com.agriculture.demo.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT配置类
 * 配置MQTT客户端的连接参数
 */
@Configuration
public class MqttConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${agriculture.mqtt.broker-address}")
    private String brokerAddress;

    @Value("${agriculture.mqtt.client-id}")
    private String clientId;

    @Value("${agriculture.mqtt.username:}")
    private String username;

    @Value("${agriculture.mqtt.password:}")
    private String password;

    @Value("${agriculture.mqtt.timeout:10}")
    private int timeout;

    @Value("${agriculture.mqtt.keepalive:60}")
    private int keepalive;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        logger.info("开始初始化MQTT客户端，Broker地址: {}", brokerAddress);
        
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient mqttClient = new MqttClient(brokerAddress, clientId, persistence);
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(timeout);
        options.setKeepAliveInterval(keepalive);
        options.setAutomaticReconnect(true);
        
        if (username != null && !username.isEmpty()) {
            options.setUserName(username);
        }
        if (password != null && !password.isEmpty()) {
            options.setPassword(password.toCharArray());
        }
        
        logger.info("MQTT客户端连接Broker...");
        mqttClient.connect(options);
        logger.info("MQTT客户端连接成功！");
        
        return mqttClient;
    }
}
