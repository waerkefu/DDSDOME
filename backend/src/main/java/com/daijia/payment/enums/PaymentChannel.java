package com.daijia.payment.enums;

import lombok.Getter;

/**
 * 支付渠道
 */
@Getter
public enum PaymentChannel {

    /** 模拟支付渠道，接口形态对齐真实渠道，便于后续替换 */
    MOCK("模拟支付"),
    WECHAT("微信支付"),
    ALIPAY("支付宝");

    private final String description;

    PaymentChannel(String description) {
        this.description = description;
    }
}
