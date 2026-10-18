package com.daijia.common;

import lombok.Getter;

/**
 * 业务异常定义类
 */
@Getter
public class BusinessException extends RuntimeException {
    //它存储这个业务异常对应的 错误码。如404等。
    private final int code;

    // 构造方法，根据 ResultCode {构造异常}
    public BusinessException(ResultCode resultCode) {
        //`BusinessException` 继承自`RuntimeException` ，
        // 而`RuntimeException` 有一个字段`detailMessage` 存储异常信息。
        // 必须通过`super(message)` 把消息传给父类，
        // 否则`e.getMessage()` 会返回`null` 。
        super(resultCode.getMessage());// 调用父类的构造方法，将异常信息传递给父类。
        this.code = resultCode.getCode();
    }

    // 构造方法，根据 枚举的ResultCode {构造异常} 和 {自定义异常信息}
    //throw new BusinessException (ResultCode.ORDER_NOT_FOUND); 
    // → code=2001, message="订单不存在"（枚举预设的）
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
    
    // 构造方法，接收 {自定义码} 和 {异常信息}
    //throw new BusinessException ( 7001 , "某个未在枚举中定义的特殊错误" ); 
    // → code=7001, message="某个未在枚举中定义的特殊错误"
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
