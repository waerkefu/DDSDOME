package com.daijia.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单相关配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.order")
public class OrderProperties {

    /** 待接单订单超时自动关闭时间（分钟） */
    private long acceptTimeoutMinutes = 15L;

    /** 预约单允许的最大提前预约天数 */
    private long maxBookingDays = 7L;
}
