package com.daijia.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 订单详情（订单主体 + 状态流转记录）
 */
@Data
public class OrderDetailVO implements Serializable {

    private OrderVO order;

    private List<OrderStatusLogVO> statusLogs;
}
