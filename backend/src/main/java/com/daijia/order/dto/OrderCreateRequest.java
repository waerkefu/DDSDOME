package com.daijia.order.dto;

import com.daijia.order.enums.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户下单请求
 */
@Data
public class OrderCreateRequest implements Serializable {

    @NotNull(message = "订单类型不能为空")
    private OrderType orderType;

    @NotBlank(message = "城市编码不能为空")
    private String cityCode;

    @NotBlank(message = "车型编码不能为空")
    private String vehicleType;

    private String vehiclePlate;

    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    @NotBlank(message = "起点地址不能为空")
    private String startAddress;

    private BigDecimal startLongitude;

    private BigDecimal startLatitude;

    private String endAddress;

    private BigDecimal endLongitude;

    private BigDecimal endLatitude;

    /** 预约用车时间：预约单 / 代办单必填 */
    private LocalDateTime bookingTime;

    @NotNull(message = "里程不能为空")
    private Integer distanceM;

    /** 预估时长（秒） */
    private Integer durationS;

    private String remark;
}
