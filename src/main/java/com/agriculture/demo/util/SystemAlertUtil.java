package com.agriculture.demo.util;

import com.agriculture.demo.enums.SystemAlertLevelEnum;
import com.agriculture.demo.enums.SystemAlertTypeEnum;
import com.agriculture.demo.service.SystemAlertLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 系统告警工具类
 * 提供便捷的告警记录方法，可在任何地方调用
 */
@Component
public class SystemAlertUtil {

    private static final Logger logger = LoggerFactory.getLogger(SystemAlertUtil.class);

    @Autowired
    private SystemAlertLogService systemAlertLogService;

    private static SystemAlertUtil instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 记录MQTT错误告警
     */
    public static void logMqttError(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.MQTT_ERROR, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 记录数据库错误告警
     */
    public static void logDatabaseError(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.DATABASE_ERROR, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 记录配置错误告警
     */
    public static void logConfigError(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.CONFIG_ERROR, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 记录定时任务错误告警
     */
    public static void logScheduleError(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.SCHEDULE_ERROR, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 记录外部接口调用失败告警
     */
    public static void logIntegrationError(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.INTEGRATION_ERROR, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 记录WebSocket错误告警
     */
    public static void logWebSocketError(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.WEBSOCKET_ERROR, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 记录资源警告
     */
    public static void logResourceWarning(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.RESOURCE_WARNING, SystemAlertLevelEnum.WARN, message, detail, source);
    }

    /**
     * 记录系统错误告警
     */
    public static void logSystemError(String message, String detail, String source) {
        logAlert(SystemAlertTypeEnum.SYSTEM_ERROR, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 记录信息级别告警
     */
    public static void logInfo(SystemAlertTypeEnum alertType, String message, String detail, String source) {
        logAlert(alertType, SystemAlertLevelEnum.INFO, message, detail, source);
    }

    /**
     * 记录警告级别告警
     */
    public static void logWarn(SystemAlertTypeEnum alertType, String message, String detail, String source) {
        logAlert(alertType, SystemAlertLevelEnum.WARN, message, detail, source);
    }

    /**
     * 记录错误级别告警
     */
    public static void logError(SystemAlertTypeEnum alertType, String message, String detail, String source) {
        logAlert(alertType, SystemAlertLevelEnum.ERROR, message, detail, source);
    }

    /**
     * 通用告警记录方法
     */
    public static void logAlert(SystemAlertTypeEnum alertType, SystemAlertLevelEnum alertLevel,
            String message, String detail, String source) {
        try {
            if (instance != null && instance.systemAlertLogService != null) {
                instance.systemAlertLogService.logAlert(alertType, alertLevel, message, detail, source);
            } else {
                // 如果Service还未初始化，先记录日志
                logger.warn("系统告警服务尚未就绪，告警消息: {} - {}", alertType.getCode(), message);
            }
        } catch (Exception e) {
            // 确保告警记录本身的异常不会影响业务
            logger.error("记录系统告警失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据异常类型记录告警
     */
    public static void logException(Exception e, String source) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            message = e.getClass().getSimpleName();
        }

        // 构建详细信息
        StringBuilder detail = new StringBuilder();
        detail.append("Exception: ").append(e.getClass().getName()).append("\n");
        detail.append("Message: ").append(e.getMessage()).append("\n");

        // 添加堆栈信息（前10行）
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < Math.min(10, stackTrace.length); i++) {
            detail.append("  at ").append(stackTrace[i].toString()).append("\n");
        }

        // 根据异常类型判断告警类型
        SystemAlertTypeEnum alertType = determineAlertType(e);
        logAlert(alertType, SystemAlertLevelEnum.ERROR, message, detail.toString(), source);
    }

    /**
     * 根据异常类型判断告警类型
     */
    private static SystemAlertTypeEnum determineAlertType(Exception e) {
        String exceptionName = e.getClass().getName();

        if (exceptionName.contains("SQLException") || exceptionName.contains("Database")) {
            return SystemAlertTypeEnum.DATABASE_ERROR;
        } else if (exceptionName.contains("Mqtt") || exceptionName.contains("MQTT")) {
            return SystemAlertTypeEnum.MQTT_ERROR;
        } else if (exceptionName.contains("WebSocket")) {
            return SystemAlertTypeEnum.WEBSOCKET_ERROR;
        } else if (exceptionName.contains("Timeout") || exceptionName.contains("ConnectException")) {
            return SystemAlertTypeEnum.INTEGRATION_ERROR;
        } else {
            return SystemAlertTypeEnum.SYSTEM_ERROR;
        }
    }
}