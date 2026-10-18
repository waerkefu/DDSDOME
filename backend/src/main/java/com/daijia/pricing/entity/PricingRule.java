package com.daijia.pricing.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 计价规则（城市 + 车型维度）
 *
 * <p>所有金额字段单位为「分」，避免浮点误差。</p>
 */
@Data
@TableName("pricing_rule")
public class PricingRule implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;//主键ID

    private String ruleName;//计价规则名称

    private String cityCode;//城市编码

    private String cityName;//城市名称

    private String vehicleType;//车型

    /** 起步价（分） */
    private Integer basePrice;

    /** 起步包含里程（米） */
    private Integer baseDistanceM;

    /** 起步包含时长（秒） */
    private Integer baseDurationS;

    /** 超出里程单价（分/公里） */
    private Integer pricePerKm;

    /** 超出时长单价（分/分钟） */
    private Integer pricePerMin;

    /** 最低消费（分） */
    private Integer minPrice;

    /** 夜间加价开始时间 */
    private LocalTime nightStart;

    /** 夜间加价结束时间 */
    private LocalTime nightEnd;

    /** 夜间加价系数 */
    private BigDecimal nightRatio;

    /** 预约加价系数 */
    private BigDecimal bookingRatio;

    private LocalDateTime effectiveTime;//生效时间

    /** 1=启用 0=禁用 */
    private Integer status;//状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;//创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;//更新时间

    @TableLogic
    private Integer deleted;//是否删除（0=未删除 1=是）
}
