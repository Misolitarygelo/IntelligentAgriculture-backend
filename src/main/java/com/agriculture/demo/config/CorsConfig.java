package com.agriculture.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 允许前端跨域访问后端接口
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许的前端来源（生产环境建议从配置文件读取）
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://127.0.0.1:8080");
        config.addAllowedOrigin("http://192.168.20.185");
        config.addAllowedOrigin("http://127.0.0.1:5500");
        config.addAllowedOrigin("http://localhost:5500");

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有请求方法（GET, POST, PUT, DELETE等）
        config.addAllowedMethod("*");

        // 允许携带凭证（WebSocket连接需要这个）
        config.setAllowCredentials(true);

        // 设置预检请求的有效期（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有接口应用跨域配置
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
