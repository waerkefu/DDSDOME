package com.daijia.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单状态流转日志视图对象
 */
@Data
public class OrderStatusLogVO implements Serializable {

    private String fromStatus;//从状态
    private String toStatus;//到状态
    private String fromStatusDesc;//从状态描述
    private String toStatusDesc;//到状态描述
    private Integer operatorType;//操作类型
    private String operatorName;//操作人姓名
    private String remark;//备注
    /** 创建时间 */
    private LocalDateTime createTime;
}
