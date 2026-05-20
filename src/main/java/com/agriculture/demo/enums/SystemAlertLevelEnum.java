package com.agriculture.demo.enums;

/**
 * 系统告警级别枚举
 */
public enum SystemAlertLevelEnum {
    
    INFO("INFO", "信息"),
    WARN("WARN", "警告"),
    ERROR("ERROR", "错误");

    private final String code;
    private final String name;

    SystemAlertLevelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static SystemAlertLevelEnum getByCode(String code) {
        for (SystemAlertLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return INFO;
    }
}
