package com.agriculture.demo.enums;

/**
 * 告警状态枚举
 */
public enum AlertStatusEnum {
    
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    DONE("DONE", "已完成"),
    IGNORED("IGNORED", "已忽略");

    private String code;
    private String name;

    AlertStatusEnum(String code, String name) {
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
    public static AlertStatusEnum getByCode(String code) {
        for (AlertStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}
