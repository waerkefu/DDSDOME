package com.daijia.order.dto;

import com.daijia.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订单分页查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderQuery extends PageQuery {

    private String status;//订单状态

    private String orderType;//订单类型

    private String orderNo;//订单编号

    private Long customerId;//客户ID

    private Long providerId;//服务商ID

    /** 创建时间起（含） */
    private LocalDateTime startTime;

    /** 创建时间止（含） */
    private LocalDateTime endTime;
}
