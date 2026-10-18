package com.daijia.order.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 取消订单请求
 */
@Data
public class CancelOrderRequest implements Serializable {

    private String reason;
}
