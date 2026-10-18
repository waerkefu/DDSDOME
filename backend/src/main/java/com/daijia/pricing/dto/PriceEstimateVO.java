package com.daijia.pricing.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 价格预估结果。
 *
 * <p>「分」为计算与存储单位，同时提供元字符串便于前端展示。</p>
 */
@Data
public class PriceEstimateVO implements Serializable {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /** 最终金额（分） */
    private Integer totalAmount;

    /** 最终金额（元） */
    private String totalAmountYuan;

    /** 起步价（分） */
    private Integer basePrice;

    /** 里程费（分，未含夜间系数） */
    private Integer distanceFee;

    /** 时长费（分，未含夜间系数） */
    private Integer durationFee;

    /** 实际生效的夜间加价系数 */
    private BigDecimal nightRatio;

    /** 实际生效的预约加价系数 */
    private BigDecimal bookingRatio;

    /** 最低消费（分） */
    private Integer minPrice;

    /** 最终金额（元） */
    public String getTotalAmountYuan() {
        if (totalAmount == null) {
            return null;
        }
        return BigDecimal.valueOf(totalAmount).divide(HUNDRED, 2, RoundingMode.HALF_UP).toPlainString();
    }
}
