package com.daijia.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构类
 * 用有语义的静态方法替代构造方法来创建对象
 * 静态工厂方法，用于创建不同类型的响应对象
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;

    /** 成功响应 */
    public static <T> Result<T> ok() {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /** 成功响应，包含数据 */
    public static <T> Result<T> ok(T data) {
        return build(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /** 失败响应 */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return build(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /** 失败响应，包含自定义消息 */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return build(resultCode.getCode(), message, null);
    }

    /** 失败响应，包含自定义错误码 */
    public static <T> Result<T> fail(int code, String message) {
        return build(code, message, null);
    }

    /** 构建响应 
     * @param code 错误码
     * @param message 错误消息
     * @param data 数据
     * @return Result<T>
     * 私有的静态方法，用于构建响应对象
    */
    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
