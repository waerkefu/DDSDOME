package com.daijia.payment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退款单
 */
@Data
@TableName("refund")
public class Refund implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;//退款单ID

    private String refundNo;//退款单号

    private Long paymentId;//支付单ID

    private String payNo;//支付单号

    private Long orderId;//订单ID

    private String orderNo;//订单号

    /** 退款金额（分） */
    private Integer amount;

    private String reason;//退款原因

    /** 见 RefundStatus */
    private String status;

    private String idempotencyKey;//幂等键，数据库唯一索引兜底

    private LocalDateTime refundTime;//退款时间

    private Long operatorId;//操作人ID

    private String remark;//备注

    @TableField(fill = FieldFill.INSERT)// 插入时填充当前时间
    private LocalDateTime createTime;//创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)// 插入或更新时填充当前时间
    private LocalDateTime updateTime;//更新时间

    @TableLogic//逻辑删除注解
    private Integer deleted;//是否删除，0：未删除，1：已删除
}
