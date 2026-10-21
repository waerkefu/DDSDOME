package com.daijia.user.controller;

import com.daijia.common.Result;
import com.daijia.security.UserContext;
import com.daijia.user.dto.CurrentUserVO;
import com.daijia.user.dto.LoginRequest;
import com.daijia.user.dto.LoginVO;
import com.daijia.user.dto.RegisterRequest;
import com.daijia.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：登录 / 注册 / 当前用户信息
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录（无需鉴权）
     * @param request 登录请求
     * @return 登录成功后的登录凭证
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    /** 注册（无需鉴权）
     * @param request 注册请求
     * @return 注册成功后的登录凭证
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    /** 当前登录用户信息（需鉴权）
     * @return 当前登录用户的VO
     */
    @GetMapping("/me")
    public Result<CurrentUserVO> me() {
        return Result.ok(authService.currentUser(UserContext.currentUserId()));
    }
}
