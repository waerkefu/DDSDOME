package com.daijia.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 订单视图对象。
 *
 * <p>金额字段以「分」存储，额外提供 xxxYuan 字符串（元，保留两位小数）便于展示。</p>
 */
@Data
public class OrderVO implements Serializable {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private Long id;//订单ID
    private String orderNo;//订单编号
    private String orderType;//订单类型
    private String orderTypeDesc;//订单类型描述
    private String status;//订单状态
    private String statusDesc;//订单状态描述
    private Long customerId;//客户ID
    private String customerName;//客户姓名
    private Long providerId;//服务商ID
    private String providerName;//服务商名称
    private String cityCode;//城市编码
    private String vehicleType;//车辆类型
    private String vehiclePlate;//车辆车牌号
    private String contactName;//联系人姓名
    private String contactPhone;//联系人手机号
    private String startAddress;//出发地址
    private String endAddress;//到达地址
    private LocalDateTime bookingTime;//预约时间    
    private LocalDateTime acceptTime;//接受时间
    private LocalDateTime serviceStartTime;//服务开始时间
    private LocalDateTime serviceEndTime;//服务结束时间
    private LocalDateTime finishTime;//完成时间
    private LocalDateTime cancelTime;//取消时间
    private String cancelReason;//取消原因
    private Integer distanceM;//距离（米）
    private Integer durationS;//持续时间（秒）
    private Integer estimateAmount;//预计金额（分）
    private Integer actualAmount;//实际金额（分）
    private Integer payAmount;//支付金额（分）
    private Integer refundAmount;//退款金额（分）
    private String payStatus;//支付状态
    private String payStatusDesc;//支付状态描述
    private String remark;//备注
    private LocalDateTime createTime;//创建时间

    /** 预计金额（元） */
    public String getEstimateAmountYuan() {
        return toYuan(estimateAmount);
    }

    /** 实际金额（元） */
    public String getActualAmountYuan() {
        return toYuan(actualAmount);
    }

    /** 支付金额（元） */
    public String getPayAmountYuan() {
        return toYuan(payAmount);
    }

    /** 退款金额（元） */
    public String getRefundAmountYuan() {
        return toYuan(refundAmount);
    }

    /** 退款金额（元） */
    private static String toYuan(Integer fen) {
        if (fen == null) {
            return null;
        }
        return BigDecimal.valueOf(fen).divide(HUNDRED, 2, RoundingMode.HALF_UP).toPlainString();
    }
}
