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
 * 支付单
 */
@Data
@TableName("payment")
public class Payment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;//支付单ID

    private String payNo;//支付单号

    private Long orderId;//订单ID

    private String orderNo;//订单号

    private Long customerId;//客户ID

    /** 支付金额（分） */
    private Integer amount;

    /** MOCK / WECHAT / ALIPAY */
    private String channel;

    /** 见 PaymentStatus */
    private String status;

    /** 幂等键，数据库唯一索引兜底 */
    private String idempotencyKey;

    /** 渠道交易流水号 */
    private String tradeNo;

    private LocalDateTime payTime;//支付时间

    private LocalDateTime notifyTime;//通知时间

    private String remark;//备注

    @TableField(fill = FieldFill.INSERT)//“自动填写”标签
    private LocalDateTime createTime;//创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)//“自动填写”标签
    private LocalDateTime updateTime;//更新时间

    @TableLogic
    private Integer deleted;//是否删除（0：未删除，1：已删除）
}
