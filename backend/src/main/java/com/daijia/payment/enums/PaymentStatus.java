package com.daijia.payment.enums;

import lombok.Getter;

/**
 * 支付单状态
 */
@Getter
public enum PaymentStatus {

    INIT("初始"),
    PAYING("支付中"),
    SUCCESS("支付成功"),
    FAILED("支付失败"),
    CLOSED("已关闭"),
    REFUNDED("已退款");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }
}
