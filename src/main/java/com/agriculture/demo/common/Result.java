package com.agriculture.demo.common;

import java.io.Serializable;

/**
 * 统一API响应封装类
 * 所有Controller返回都使用此包装类，便于前后端交互
 *
 * @param <T> 响应数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private int code;

    /** 消息 */
    private String message;

    /** 响应数据 */
    private T data;

    // ============================== 构造方法 ==============================
    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ============================== 静态工厂方法 ==============================
    /**
     * 成功响应（默认）
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应（默认）
     */
    public static <T> Result<T> fail() {
        return new Result<>(500, "操作失败", null);
    }

    /**
     * 失败响应（带消息）
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败响应（带状态码和消息）
     */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 参数校验失败响应
     */
    public static <T> Result<T> validateFail(String message) {
        return new Result<>(400, message, null);
    }

    /**
     * 失败响应（默认，error别名）
     */
    public static <T> Result<T> error() {
        return fail();
    }

    /**
     * 失败响应（带消息，error别名）
     */
    public static <T> Result<T> error(String message) {
        return fail(message);
    }

    /**
     * 失败响应（带状态码和消息，error别名）
     */
    public static <T> Result<T> error(int code, String message) {
        return fail(code, message);
    }

    // ============================== Getter and Setter
    // ==============================
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
