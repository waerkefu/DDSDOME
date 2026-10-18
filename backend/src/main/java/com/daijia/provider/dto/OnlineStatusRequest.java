package com.daijia.provider.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 服务商在线状态修改请求
 */
@Data
public class OnlineStatusRequest implements Serializable {

    /** 0=离线 1=在线 */
    @NotNull(message = "在线状态不能为空")
    private Integer onlineStatus;
}
