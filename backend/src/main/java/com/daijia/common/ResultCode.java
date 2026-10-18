package com.daijia.common;

/**
 * 统一业务错误码枚举类（字典）
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统繁忙，请稍后重试"),

    /** 账号相关 */
    ACCOUNT_NOT_FOUND(1001, "账号不存在"),
    ACCOUNT_PASSWORD_ERROR(1002, "账号或密码错误"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),
    ACCOUNT_EXISTS(1004, "账号或手机号已被注册"),
    PROVIDER_NOT_FOUND(1005, "服务商档案不存在"),
    PROVIDER_NOT_AVAILABLE(1006, "服务商当前不可接单"),

    /** 订单相关 */
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_NOT_ALLOWED(2002, "订单当前状态不允许该操作"),
    ORDER_ALREADY_ACCEPTED(2003, "订单已被接单"),
    ORDER_VERSION_CONFLICT(2004, "订单已被其他操作修改，请重试"),
    ORDER_NOT_BELONG_TO_YOU(2005, "无权操作该订单"),
    ORDER_PARAM_INVALID(2006, "订单参数不合法"),
    BOOKING_TIME_INVALID(2007, "预约时间不合法"),

    /** 计价相关 */
    PRICING_RULE_NOT_FOUND(3001, "未找到匹配的计价规则"),
    PRICING_RULE_EXISTS(3002, "该城市与车型已存在计价规则"),

    /** 支付相关 */
    PAYMENT_NOT_FOUND(4001, "支付单不存在"),
    PAYMENT_STATUS_NOT_ALLOWED(4002, "订单当前状态不允许支付"),
    PAYMENT_AMOUNT_MISMATCH(4003, "支付金额与订单金额不一致"),
    REFUND_AMOUNT_EXCEED(4004, "退款金额超出可退范围"),

    /** 通用 */
    DUPLICATE_REQUEST(5001, "重复请求，请勿重复提交");

    private final int code;
    private final String message;

    /** 构造方法，初始化错误码和消息 */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
