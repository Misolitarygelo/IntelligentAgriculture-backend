package com.agriculture.demo.common;

/**
 * 自定义业务异常类
 * 用于处理业务逻辑中的异常情况
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    // ============================== 构造方法 ==============================
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    // ============================== Getter and Setter ==============================
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
