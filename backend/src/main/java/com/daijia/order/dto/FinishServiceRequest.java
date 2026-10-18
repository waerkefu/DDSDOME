package com.daijia.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 结束服务请求：以实际里程与时长结算
 */
@Data
public class FinishServiceRequest implements Serializable {

    @NotNull(message = "实际里程不能为空")
    private Integer distanceM;

    @NotNull(message = "实际时长不能为空")
    private Integer durationS;
}
