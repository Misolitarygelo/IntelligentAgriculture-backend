package com.agriculture.demo.enums;

/**
 * 系统告警处理状态枚举
 */
public enum SystemAlertStatusEnum {

  PENDING("PENDING", "待处理"),
  PROCESSING("PROCESSING", "处理中"),
  DONE("DONE", "已处理"),
  IGNORED("IGNORED", "已忽略");

  private final String code;
  private final String name;

  SystemAlertStatusEnum(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public static SystemAlertStatusEnum getByCode(String code) {
    for (SystemAlertStatusEnum status : values()) {
      if (status.getCode().equals(code)) {
        return status;
      }
    }
    return PENDING;
  }
}
