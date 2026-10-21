package com.daijia.security;

import com.daijia.common.BusinessException;
import com.daijia.common.ResultCode;
import com.daijia.user.entity.SysUser;
import com.daijia.user.mapper.SysPermissionMapper;
import com.daijia.user.mapper.SysRoleMapper;
import com.daijia.user.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 认证与鉴权拦截器：
 * 1. 解析并校验 JWT；
 * 2. 校验账号是否存在且未被禁用（保证禁用账号即时生效）；
 * 3. 按 {@link RequireRole} 注解做角色校验；
 * 4. 将登录用户写入 UserContext，请求结束后清理。
 */
@Slf4j
@Component// 注册为 Spring 组件
@RequiredArgsConstructor// 构造函数注入依赖
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ROLE_CUSTOMER = "CUSTOMER";// 客户角色
    public static final String ROLE_PROVIDER = "PROVIDER";// 供应商角色
    public static final String ROLE_ADMIN = "ADMIN";// 管理员角色

    private final JwtUtil jwtUtil;// JWT 工具类
    private final JwtProperties jwtProperties;// JWT 配置属性
    private final SysUserMapper sysUserMapper;// 用户映射器
    private final SysRoleMapper sysRoleMapper;// 角色映射器
    private final SysPermissionMapper sysPermissionMapper;// 权限映射器

    /**
     * 拦截请求前调用，用于校验 JWT 令牌
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 处理器（Controller 方法）
     * @return 是否继续处理请求（true=继续，false=中断）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 如果 是 OPTIONS 请求 或 不是 Controller 处理器，直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || !(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 从请求头提取令牌
        //`Bearer` 不是令牌的一部分，它只是“凭证类型”的标签 。
        String token = this.resolveToken(request);
        // 如果令牌为空，抛出异常
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 解析令牌
        //Claims 是 JWT 有效载荷，包含了用户信息，以 JSON 格式存储。
        Claims claims = jwtUtil.parseToken(token);
        Long userId;// 用户 ID
        try {
            // 从令牌中提取用户 ID
            userId = Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 校验用户是否存在且未被禁用
        SysUser user = sysUserMapper.selectById(userId);
        // 如果用户不存在，抛出异常
        if (user == null) {
            throw new BusinessException(ResultCode.ACCOUNT_NOT_FOUND);
        }
        // 如果用户状态不是启用，抛出异常
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 构建登录用户上下文
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setUserType(user.getUserType());

        // 从数据库查询用户角色和权限
        Set<String> roles = new HashSet<>(sysRoleMapper.selectRoleCodesByUserId(userId));
        loginUser.setRoles(roles);
        loginUser.setPermissions(new HashSet<>(sysPermissionMapper.selectPermCodesByUserId(userId)));
        
        // 校验用户角色是否匹配要求
        RequireRole requireRole = resolveAnnotation(handlerMethod, RequireRole.class);
        // 如果指定了角色，检查用户角色是否包含指定角色
        if (requireRole != null && !matchRole(roles, requireRole)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 将登录用户写入 UserContext
        UserContext.set(loginUser);
        return true;
    }

    /** 请求结束后清理 UserContext */
     @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 从请求头提取令牌，兼容带与不带 Bearer 前缀两种写法 */
    private String resolveToken(HttpServletRequest request) {
        // 从请求头提取令牌
        String header = request.getHeader(jwtProperties.getHeader());
        // 如果令牌为空，返回 null
        if (!StringUtils.hasText(header)) {
            return null;
        }

        // 取出令牌前缀
        String prefix = jwtProperties.getPrefix();
        // 如果令牌前缀存在且令牌以前缀开头，返回令牌（去掉前缀）
        //.hasText() 不是`null`,不是空串`""` （`length() > 0` ）,不是纯空白字符。返回true
        if (StringUtils.hasText(prefix) && header.startsWith(prefix)) {
            // 去掉前缀并返回令牌
            return header.substring(prefix.length()).trim();
        }
        // 如果令牌不以前缀开头，返回令牌（去掉空格）
        return header.trim();
    }

    /** 方法上有注解用方法上的，否则回退到类上的 */
    private <A extends java.lang.annotation.Annotation> A resolveAnnotation(HandlerMethod handlerMethod, Class<A> type) {
        // 从方法上获取注解
        A annotation = handlerMethod.getMethodAnnotation(type);
        // 如果方法上有注解，返回注解
        if (annotation != null) {
            return annotation;
        }
        // 如果方法上没有注解，从类上获取注解
        return handlerMethod.getBeanType().getAnnotation(type);// 从类上获取注解
        // 如果类上也没有注解，返回 null（默认值）
    }

    /** 检查用户角色是否匹配要求 */
    private boolean matchRole(Set<String> roles, RequireRole requireRole) {
        // 如果没有指定角色，返回 true
        if (requireRole.value().length == 0) {
            return true;
        }
        // 如果指定了角色，检查用户角色是否包含指定角色
        // 如果指定了角色且要求同时具备全部角色，检查用户角色是否包含所有角色
        if (requireRole.requireAll()) {
            return roles.containsAll(Arrays.asList(requireRole.value()));
        }
        // 如果指定了角色且不要求同时具备全部角色，检查用户角色是否包含至少一个角色
        return Arrays.stream(requireRole.value()).anyMatch(roles::contains);
    }
}