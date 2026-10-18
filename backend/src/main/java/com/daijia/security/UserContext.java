package com.daijia.security;

import com.daijia.common.BusinessException;
import com.daijia.common.ResultCode;

/**
 * 登录用户上下文（基于 ThreadLocal）
 * <p>
 * Controller / Service 中通过 UserContext.currentUserId() 获取当前登录人，
 * 拦截器在请求结束后调用 clear() 防止线程复用导致的数据串号。
 */
public final class UserContext {

    // 线程安全的登录用户上下文
    // 每个线程都有一个独立的登录用户实例，互不干扰
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前用户，未登录时直接抛 401 业务异常 */
    public static LoginUser require() {
        // 从上下文获取登录用户
        LoginUser loginUser = HOLDER.get();
        // 如果上下文为空，直接抛 401 业务异常
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return loginUser;
    }

    /** 获取当前登录人ID */
    public static Long currentUserId() {
        return require().getUserId();
    }

    /** 清除当前登录用户上下文 */
    public static void clear() {
        HOLDER.remove();
    }
}