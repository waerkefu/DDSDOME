package com.daijia.provider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务商接单开关修改请求
 */
@Data
public class AcceptSwitchRequest implements Serializable {

    /** 0=停止接单 1=接单中 */
    @NotNull(message = "接单开关不能为空")
    private Integer acceptEnabled;
}
