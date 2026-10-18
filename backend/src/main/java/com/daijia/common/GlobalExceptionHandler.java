package com.daijia.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/**
 * 全局异常处理类：统一转换为 Result 结构，避免异常堆栈直接暴露给前端
 * 异常处理器
 */
@Slf4j// 日志注解，用于记录异常信息
@RestControllerAdvice// 全局异常处理类，用于处理所有控制器抛出的异常
public class GlobalExceptionHandler {

    /** 处理 BusinessException异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** 处理 MethodArgumentNotValidException异常 
     * 参数校验失败时自动抛出的异常
     * 当 Controller 方法的参数上标了`@Valid` 或`@Validated` ，
     * 并且参数里某个字段没通过校验注解（`@NotNull` 、`@NotBlank` 、`@Size` 等）时，
     * Spring 就会抛出这个异常
     * `@RequestBody` 接收的 JSON 参数校验失败
    */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return Result.fail(ResultCode.BAD_REQUEST, firstErrorMessage(e.getBindingResult().getFieldError()));
    }

    /** 处理 BindException异常 
     * 也是参数校验失败时抛出的异常，
     * 但它和`MethodArgumentNotValidException` 的区别在于 参数的传递方式不同
     * 表单参数 / 查询参数绑定到对象时校验失败
    */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return Result.fail(ResultCode.BAD_REQUEST, firstErrorMessage(e.getBindingResult().getFieldError()));
    }

    /** 处理 ConstraintViolationException异常 
     * 方法参数上直接写校验注解时，抛出的异常
    */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        return Result.fail(ResultCode.BAD_REQUEST, message);
    }

    /** 处理 MissingServletRequestParameterException异常 
     * 缺少必要参数时抛出的异常
    */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(ResultCode.BAD_REQUEST, "缺少必要参数：" + e.getParameterName());
    }

    /** 处理 HttpMessageNotReadableException异常 
     * 请求体格式错误时抛出的异常
    */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.fail(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    /** 处理 Exception异常 
     * 捕获所有未处理的异常，记录错误日志并返回系统错误
    */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }

    /** 从 FieldError 中提取第一个错误消息 
     * 参数校验失败后提取错误提示文字
    */
    private String firstErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return ResultCode.BAD_REQUEST.getMessage();
        }
        return fieldError.getDefaultMessage();
    }
}
