package com.daijia.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 退款请求
 */
@Data
public class RefundRequest implements Serializable {

    /** 退款金额（分） */
    @NotNull(message = "退款金额不能为空")
    private Integer amount;

    private String reason;//退款原因
}
