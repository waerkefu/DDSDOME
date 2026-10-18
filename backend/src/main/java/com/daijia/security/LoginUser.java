package com.daijia.security;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

/**
 * 当前登录用户上下文信息
 */
@Data
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    /** 1=客户 2=服务商 3=管理员 */
    private Integer userType;
    private Set<String> roles = Collections.emptySet();
    private Set<String> permissions = Collections.emptySet();

    /** 检查用户是否具备指定角色 */
    public boolean hasRole(String roleCode) {
        return roles != null && roles.contains(roleCode);
    }
}