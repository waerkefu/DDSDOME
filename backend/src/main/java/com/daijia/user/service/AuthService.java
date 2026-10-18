package com.daijia.user.service;

import com.daijia.user.dto.CurrentUserVO;
import com.daijia.user.dto.LoginRequest;
import com.daijia.user.dto.LoginVO;
import com.daijia.user.dto.RegisterRequest;

/**
 * 认证服务：登录 / 注册 / 当前用户信息
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(LoginRequest request);

    /**
     * 注册（默认注册为客户）
     */
    LoginVO register(RegisterRequest request);

    /**
     * 查询当前登录用户信息（含角色与权限编码）
     */
    CurrentUserVO currentUser(Long userId);
}
