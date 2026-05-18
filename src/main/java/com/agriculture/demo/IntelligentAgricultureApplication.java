package com.agriculture.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智慧农业后端系统主启动类
 * 
 * @SpringBootApplication 标注这是Spring Boot应用
 * @MapperScan 指定MyBatis-Plus的Mapper扫描路径
 * @EnableScheduling 启用定时任务支持
 */
@SpringBootApplication
@MapperScan("com.agriculture.demo.mapper")
@EnableScheduling
public class IntelligentAgricultureApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelligentAgricultureApplication.class, args);
        System.out.println("==========================================");
        System.out.println("  智慧农业后端系统启动成功！");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("==========================================");
    }
}
