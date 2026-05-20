package com.agriculture.demo.enums;

/**
 * 系统告警类型枚举
 */
public enum SystemAlertTypeEnum {
    
    MQTT_ERROR("MQTT_ERROR", "MQTT连接异常"),
    DATABASE_ERROR("DATABASE_ERROR", "数据库异常"),
    CONFIG_ERROR("CONFIG_ERROR", "配置错误"),
    SCHEDULE_ERROR("SCHEDULE_ERROR", "定时任务异常"),
    INTEGRATION_ERROR("INTEGRATION_ERROR", "外部接口调用失败"),
    WEBSOCKET_ERROR("WEBSOCKET_ERROR", "WebSocket连接异常"),
    RESOURCE_WARNING("RESOURCE_WARNING", "资源告警"),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统错误");

    private final String code;
    private final String name;

    SystemAlertTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static SystemAlertTypeEnum getByCode(String code) {
        for (SystemAlertTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SYSTEM_ERROR;
    }
}
