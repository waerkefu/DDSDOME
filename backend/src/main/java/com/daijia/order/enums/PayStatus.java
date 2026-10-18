package com.daijia.order.enums;

import lombok.Getter;

/**
 * 订单支付状态
 */
@Getter
public enum PayStatus {

    UNPAID("未支付"),
    PAYING("支付中"),
    PAID("已支付"),
    REFUNDING("退款中"),
    PARTIAL_REFUNDED("部分退款"),
    REFUNDED("已退款");

    private final String description;

    PayStatus(String description) {
        this.description = description;
    }
}
