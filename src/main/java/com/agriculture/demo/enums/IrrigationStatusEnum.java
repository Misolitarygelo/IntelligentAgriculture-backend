package com.agriculture.demo.enums;

/**
 * 灌溉状态枚举
 */
public enum IrrigationStatusEnum {
    
    OPEN("OPEN", "已开启"),
    CLOSED("CLOSED", "已关闭");

    private String code;
    private String name;

    IrrigationStatusEnum(String code, String name) {
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
    public static IrrigationStatusEnum getByCode(String code) {
        for (IrrigationStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
