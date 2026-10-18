package com.daijia.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色新增 / 修改请求
 */
@Data
public class RoleSaveRequest implements Serializable {

    /** 角色编码，新增时必填 */
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;// 角色描述
}
