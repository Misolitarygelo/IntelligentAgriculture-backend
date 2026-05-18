package com.agriculture.demo.enums;

/**
 * 传感器数据类型枚举
 */
public enum DataTypeEnum {
    
    SOIL_MOISTURE("SOIL_MOISTURE", "土壤湿度"),
    AIR_TEMP("AIR_TEMP", "空气温度");

    private String code;
    private String name;

    DataTypeEnum(String code, String name) {
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
    public static DataTypeEnum getByCode(String code) {
        for (DataTypeEnum typeEnum : values()) {
            if (typeEnum.getCode().equals(code)) {
                return typeEnum;
            }
        }
        return null;
    }
}
