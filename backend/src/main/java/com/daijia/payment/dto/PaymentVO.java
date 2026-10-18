package com.daijia.payment.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付单视图对象
 */
@Data
public class PaymentVO implements Serializable {

    private Long id;//支付单ID

    private String payNo;//支付单号

    private String orderNo;//订单号

    /** 支付金额（分） */
    private Integer amount;

    /** 支付金额（元） */
    private String amountYuan;

    private String channel;//支付渠道

    private String status;//支付状态

    private LocalDateTime payTime;//支付时间

    private LocalDateTime createTime;//创建时间
}
