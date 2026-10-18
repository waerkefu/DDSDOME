package com.daijia.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色信息
 */
@Data
public class RoleVO implements Serializable {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    /** 1=启用 0=禁用 */
    private Integer status;
}
