package com.agriculture.demo.enums;

/**
 * 告警类型枚举
 */
public enum AlertTypeEnum {
    
    TEMP_LOW("TEMP_LOW", "温度过低"),
    TEMP_HIGH("TEMP_HIGH", "温度过高"),
    MOISTURE_LOW("MOISTURE_LOW", "湿度过低"),
    MOISTURE_HIGH("MOISTURE_HIGH", "湿度过高"),
    DEVICE_OFFLINE("DEVICE_OFFLINE", "设备离线");

    private String code;
    private String name;

    AlertTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据code获取枚举
     */
    public static AlertTypeEnum getByCode(String code) {
        for (AlertTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
