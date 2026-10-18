package com.daijia.order.enums;

import lombok.Getter;

/**
 * 订单状态
 */
@Getter
public enum OrderStatus {

    PENDING_ACCEPT("待接单"),
    ACCEPTED("已接单"),
    IN_SERVICE("服务中"),
    PENDING_PAYMENT("待支付"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    CLOSED("超时关闭");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    /** 是否为终态，终态不可再流转 */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == CANCELLED || this == CLOSED;
    }

    public static OrderStatus of(String name) {
        if (name == null) {
            return null;
        }
        for (OrderStatus value : values()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        return null;
    }
}
