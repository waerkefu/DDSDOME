package com.daijia.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户角色分配请求
 */
@Data
public class UserRoleRequest implements Serializable {

    @NotNull(message = "角色 ID 列表不能为空")
    private List<Long> roleIds;
}
