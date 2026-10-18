package com.daijia.payment.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款单视图对象
 */
@Data
public class RefundVO implements Serializable {

    private Long id;//退款单ID

    private String refundNo;//退款单号

    private String orderNo;//订单号

    private String payNo;//支付单号

    /** 退款金额（分） */
    private Integer amount;

    /** 退款金额（元） */
    private String amountYuan;

    private String status;//退款状态

    private String reason;//退款原因

    private LocalDateTime refundTime;//退款时间

    private LocalDateTime createTime;//创建时间
}
