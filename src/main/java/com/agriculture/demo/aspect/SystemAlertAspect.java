package com.agriculture.demo.aspect;

import com.agriculture.demo.util.SystemAlertUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 系统告警切面
 * 自动捕获异常并记录系统告警
 */
@Aspect
@Component
public class SystemAlertAspect {

    private static final Logger logger = LoggerFactory.getLogger(SystemAlertAspect.class);

    /**
     * 定义切点：所有Service层方法
     */
    @Pointcut("execution(* com.agriculture.demo.service..*.*(..))")
    public void serviceMethods() {}

    /**
     * 定义切点：所有Controller层方法
     */
    @Pointcut("execution(* com.agriculture.demo.controller..*.*(..))")
    public void controllerMethods() {}

    /**
     * 定义切点：所有MQTT相关方法
     */
    @Pointcut("execution(* com.agriculture.demo.mqtt..*.*(..))")
    public void mqttMethods() {}

    /**
     * 环绕通知：捕获异常并记录告警
     */
    @Around("serviceMethods() || controllerMethods() || mqttMethods()")
    public Object logAlertOnException(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String source = className + "." + methodName;

        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            logger.error("方法执行异常 - {}.{}: {}", className, methodName, e.getMessage(), e);
            
            // 根据异常类型记录不同类型的告警
            recordAlertByExceptionType(e, source);
            
            throw e;
        }
    }

    /**
     * 根据异常类型记录告警
     */
    private void recordAlertByExceptionType(Exception e, String source) {
        String exceptionName = e.getClass().getName();
        
        // 数据库异常
        if (isDatabaseException(e)) {
            SystemAlertUtil.logDatabaseError("数据库操作失败", 
                buildExceptionDetail(e), source);
        }
        // MQTT异常
        else if (isMqttException(e)) {
            SystemAlertUtil.logMqttError("MQTT操作失败", 
                buildExceptionDetail(e), source);
        }
        // WebSocket异常
        else if (isWebSocketException(e)) {
            SystemAlertUtil.logWebSocketError("WebSocket操作失败", 
                buildExceptionDetail(e), source);
        }
        // 网络/外部接口调用异常
        else if (isIntegrationException(e)) {
            SystemAlertUtil.logIntegrationError("外部接口调用失败", 
                buildExceptionDetail(e), source);
        }
        // 配置异常
        else if (isConfigException(e)) {
            SystemAlertUtil.logConfigError("配置错误", 
                buildExceptionDetail(e), source);
        }
        // 通用系统错误
        else {
            SystemAlertUtil.logSystemError("系统错误", 
                buildExceptionDetail(e), source);
        }
    }

    /**
     * 判断是否为数据库异常
     */
    private boolean isDatabaseException(Exception e) {
        String name = e.getClass().getName();
        return name.contains("SQLException") 
                || name.contains("Database")
                || name.contains("MyBatis")
                || name.contains("SQL")
                || name.contains("Jdbc")
                || name.contains("Transaction")
                || (e.getCause() != null && e.getCause() instanceof Exception 
                    && isDatabaseException((Exception) e.getCause()));
    }

    /**
     * 判断是否为MQTT异常
     */
    private boolean isMqttException(Exception e) {
        String name = e.getClass().getName();
        return name.contains("Mqtt") || name.contains("MQTT");
    }

    /**
     * 判断是否为WebSocket异常
     */
    private boolean isWebSocketException(Exception e) {
        String name = e.getClass().getName();
        return name.contains("WebSocket") 
                || name.contains("Stomp")
                || name.contains("Messaging");
    }

    /**
     * 判断是否为外部接口调用异常
     */
    private boolean isIntegrationException(Exception e) {
        String name = e.getClass().getName();
        return name.contains("Timeout") 
                || name.contains("ConnectException")
                || name.contains("SocketTimeout")
                || name.contains("Http")
                || name.contains("RestClient")
                || name.contains("HttpClient");
    }

    /**
     * 判断是否为配置异常
     */
    private boolean isConfigException(Exception e) {
        String name = e.getClass().getName();
        return name.contains("Config") 
                || name.contains("Property")
                || name.contains("Configuration")
                || name.contains("Yaml")
                || name.contains("JsonParse");
    }

    /**
     * 构建异常详细信息
     */
    private String buildExceptionDetail(Exception e) {
        StringBuilder detail = new StringBuilder();
        detail.append("Exception: ").append(e.getClass().getName()).append("\n");
        detail.append("Message: ").append(e.getMessage()).append("\n");
        
        // 添加堆栈信息（前5行）
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < Math.min(5, stackTrace.length); i++) {
            detail.append("  at ").append(stackTrace[i].toString()).append("\n");
        }
        
        return detail.toString();
    }
}