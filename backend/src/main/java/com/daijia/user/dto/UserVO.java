package com.daijia.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息（不含密码）
 */
@Data
public class UserVO implements Serializable {

    private Long id;

    private String username;

    private String phone;

    private String nickname;

    private String avatar;

    /** 1=客户 2=服务商 3=管理员 */
    private Integer userType;

    /** 用户类型描述 */
    private String userTypeDesc;

    /** 1=启用 0=禁用 */
    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    /** 角色编码集合 */
    private List<String> roles;
}