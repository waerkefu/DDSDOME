package com.daijia.order.enums;

import lombok.Getter;

/**
 * 订单类型
 */
@Getter
public enum OrderType {

    /** 即时代驾 */
    IMMEDIATE("即时代驾"),
    /** 预约代驾 */
    SCHEDULED("预约代驾"),
    /** 年审代办 */
    INSPECTION("年审代办"),
    /** 换证代办 */
    RENEWAL("换证代办");

    private final String description;//订单类型描述

    OrderType(String description) {
        this.description = description;
    }

    /** 是否为代办类订单 */
    public boolean isAgency() {
        return this == INSPECTION || this == RENEWAL;
    }
}
