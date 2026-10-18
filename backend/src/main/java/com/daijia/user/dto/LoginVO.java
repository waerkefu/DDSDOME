package com.daijia.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录结果
 */
@Data
public class LoginVO implements Serializable {

    /** JWT 令牌 */
    private String token;

    /** 令牌类型，固定 Bearer */
    private String tokenType = "Bearer";

    /** 有效期（秒） */
    private Long expiresIn;

    /** 登录用户信息 */
    private UserVO user;
}