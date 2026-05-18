package com.agriculture.demo.enums;

/**
 * 操作来源枚举
 */
public enum OperationSourceEnum {
    
    MANUAL("MANUAL", "手动操作"),
    AUTO("AUTO", "自动操作");

    private String code;
    private String name;

    OperationSourceEnum(String code, String name) {
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
    public static OperationSourceEnum getByCode(String code) {
        for (OperationSourceEnum sourceEnum : values()) {
            if (sourceEnum.getCode().equals(code)) {
                return sourceEnum;
            }
        }
        return null;
    }
}
