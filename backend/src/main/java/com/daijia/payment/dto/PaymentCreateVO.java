package com.daijia.payment.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建支付单结果（模拟支付渠道，形态对齐真实渠道）
 */
@Data
public class PaymentCreateVO implements Serializable {

    private String payNo;//支付单号

    private String orderNo;//订单号

    /** 支付金额（分） */
    private Integer amount;

    /** 支付金额（元） */
    private String amountYuan;

    /** 支付渠道，模拟支付固定为 MOCK */
    private String channel;

    private String status;//支付状态

    /** 模拟支付回调地址 */
    private String mockPayUrl;

    /** 支付单有效时间（秒） */
    private Integer expireSeconds;
}
