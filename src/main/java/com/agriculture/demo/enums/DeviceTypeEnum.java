package com.agriculture.demo.enums;

/**
 * 设备类型枚举
 */
public enum DeviceTypeEnum {
    
    MOISTURE_TEMP("MOISTURE_TEMP", "温湿度传感器"),
    IRRIGATION("IRRIGATION", "灌溉控制器");

    private String code;
    private String name;

    DeviceTypeEnum(String code, String name) {
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
    public static DeviceTypeEnum getByCode(String code) {
        for (DeviceTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
