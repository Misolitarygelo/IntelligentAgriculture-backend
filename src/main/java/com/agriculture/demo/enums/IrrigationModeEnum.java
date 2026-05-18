package com.agriculture.demo.enums;

/**
 * 灌溉模式枚举
 */
public enum IrrigationModeEnum {
    
    MANUAL("MANUAL", "手动模式"),
    AUTO("AUTO", "自动模式");

    private String code;
    private String name;

    IrrigationModeEnum(String code, String name) {
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
    public static IrrigationModeEnum getByCode(String code) {
        for (IrrigationModeEnum modeEnum : values()) {
            if (modeEnum.getCode().equals(code)) {
                return modeEnum;
            }
        }
        return null;
    }
}
