package com.daijia.order.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表。
 *
 * <p>表名使用 orders 以规避 MySQL 保留字 ORDER。</p>
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号（业务唯一） */
    private String orderNo;

    /** IMMEDIATE / SCHEDULED / INSPECTION / RENEWAL */
    private String orderType;

    /** 见 OrderStatus */
    private String status;

    private Long customerId;

    /** 接单服务商 ID，未接单为空 */
    private Long providerId;

    private String cityCode;

    private String vehicleType;

    private String vehiclePlate;

    private String contactName;

    private String contactPhone;

    private String startAddress;

    private BigDecimal startLongitude;

    private BigDecimal startLatitude;

    private String endAddress;

    private BigDecimal endLongitude;

    private BigDecimal endLatitude;

    /** 预约用车时间 */
    private LocalDateTime bookingTime;

    private LocalDateTime acceptTime;

    private LocalDateTime serviceStartTime;

    private LocalDateTime serviceEndTime;

    private LocalDateTime finishTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    /** 里程（米） */
    private Integer distanceM;

    /** 时长（秒） */
    private Integer durationS;

    /** 预估金额（分） */
    private Integer estimateAmount;

    /** 实际结算金额（分） */
    private Integer actualAmount;

    /** 已支付金额（分） */
    private Integer payAmount;

    /** 已退款金额（分） */
    private Integer refundAmount;

    /** 见 PayStatus */
    private String payStatus;

    private String remark;

    /** 待接单超时时间 */
    private LocalDateTime expireTime;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
