package com.daijia.pricing.dto;

import com.daijia.order.enums.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 价格预估请求
 */
@Data
public class PriceEstimateRequest implements Serializable {

    @NotNull(message = "订单类型不能为空")
    private OrderType orderType;

    @NotBlank(message = "城市编码不能为空")
    private String cityCode;

    @NotBlank(message = "车型编码不能为空")
    private String vehicleType;

    /** 预估里程（米） */
    @NotNull(message = "里程不能为空")
    private Integer distanceM;

    /** 预估时长（秒） */
    private Integer durationS;

    /** 预约用车时间，即时代驾可为空 */
    private LocalDateTime bookingTime;
}
