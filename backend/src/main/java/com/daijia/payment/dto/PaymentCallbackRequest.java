package com.daijia.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 模拟支付网关回调请求
 */
@Data
public class PaymentCallbackRequest implements Serializable {

    @NotBlank(message = "支付单号不能为空")
    private String payNo;

    @NotNull(message = "支付结果不能为空")
    private Boolean success;

    /** 渠道交易流水号，可选 */
    private String tradeNo;
}
