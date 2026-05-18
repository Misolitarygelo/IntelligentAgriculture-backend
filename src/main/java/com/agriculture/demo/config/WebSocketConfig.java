package com.agriculture.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * WebSocket配置类
 * 使用STOMP协议实现消息推送
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理
        config.enableSimpleBroker("/topic");
        // 应用目标前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，允许跨域
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:8080",
                        "http://127.0.0.1:8080",
                        "http://192.168.20.185",
                        "http://127.0.0.1:5500",
                        "http://localhost:5500")
                .withSockJS();
    }
}
