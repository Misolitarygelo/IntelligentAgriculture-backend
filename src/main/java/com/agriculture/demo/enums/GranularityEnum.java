package com.agriculture.demo.enums;

/**
 * 数据粒度枚举
 */
public enum GranularityEnum {
    SECOND("second", "秒"),
    MINUTE("minute", "分钟"),
    HOUR("hour", "小时"),
    DAY("day", "天");

    private String code;
    private String name;

    GranularityEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static GranularityEnum fromCode(String code) {
        for (GranularityEnum enumValue : values()) {
            if (enumValue.getCode().equalsIgnoreCase(code)) {
                return enumValue;
            }
        }
        return MINUTE; // 默认按分钟
    }
}