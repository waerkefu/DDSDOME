package com.daijia.pricing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 计价规则新增 / 修改请求
 */
@Data
public class PricingRuleSaveRequest implements Serializable {

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "城市编码不能为空")
    private String cityCode;

    private String cityName;

    @NotBlank(message = "车型编码不能为空")
    private String vehicleType;

    @NotNull(message = "起步价不能为空")
    private Integer basePrice;

    private Integer baseDistanceM;

    private Integer baseDurationS;

    private Integer pricePerKm;

    private Integer pricePerMin;

    private Integer minPrice;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime nightStart;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime nightEnd;

    private BigDecimal nightRatio;

    private BigDecimal bookingRatio;

    /** 1=启用 0=禁用 */
    private Integer status;
}
