package com.daijia.payment.enums;

import lombok.Getter;

/**
 * 退款单状态
 */
@Getter
public enum RefundStatus {

    INIT("初始"),
    PROCESSING("退款中"),
    SUCCESS("退款成功"),
    FAILED("退款失败");

    private final String description;

    RefundStatus(String description) {
        this.description = description;
    }
}
