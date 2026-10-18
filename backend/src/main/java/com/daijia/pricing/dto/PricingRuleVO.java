package com.daijia.pricing.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 计价规则视图对象。
 *
 * <p>金额字段以「分」存储，额外提供 xxxYuan 字符串（元，保留两位小数）便于展示。</p>
 */
@Data
public class PricingRuleVO implements Serializable {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private Long id;// 计价规则ID
    private String ruleName;// 计价规则名称
    private String cityCode;// 城市编码
    private String cityName;// 城市名称
    private String vehicleType;// 车辆类型  
    private Integer basePrice;// 基础价格（分）
    private Integer baseDistanceM;// 基础距离（米）
    private Integer baseDurationS;// 基础时间（秒）
    private Integer pricePerKm;// 每公里价格（分）
    private Integer pricePerMin;// 每分钟价格（分）
    private Integer minPrice;// 最低价格（分）
    private LocalTime nightStart;// 夜间开始时间
    private LocalTime nightEnd;// 夜间结束时间
    private BigDecimal nightRatio;// 夜间价格比例
    private BigDecimal bookingRatio;// 预约价格比例
    private Integer status;// 状态
    /** 创建时间 */
    private LocalDateTime createTime;

    public String getBasePriceYuan() {
        return toYuan(basePrice);
    }

    public String getPricePerKmYuan() {
        return toYuan(pricePerKm);
    }

    public String getPricePerMinYuan() {
        return toYuan(pricePerMin);
    }

    public String getMinPriceYuan() {
        return toYuan(minPrice);
    }

    private static String toYuan(Integer fen) {
        if (fen == null) {
            return null;
        }
        return BigDecimal.valueOf(fen).divide(HUNDRED, 2, RoundingMode.HALF_UP).toPlainString();
    }
}
