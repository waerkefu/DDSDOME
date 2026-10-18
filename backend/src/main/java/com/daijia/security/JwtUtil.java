package com.daijia.security;

import com.daijia.common.BusinessException;
import com.daijia.common.ResultCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌生成与解析
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    // 生成密钥对
    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成令牌：subject 存 userId，附加 username / userType 两个自定义声明
     * 服务器在登录成功那一刻为客户端生成的“身份证”
     */
    public String generateToken(Long userId, String username, Integer userType) {
        long now = System.currentTimeMillis();// 当前时间
        Date expiration = new Date(now + jwtProperties.getExpireMinutes() * 60 * 1000);// 过期时间
        // 构建令牌
        return Jwts.builder()
                .subject(String.valueOf(userId))// 存储用户 ID
                .claim("username", username)// 存储用户名
                .claim("userType", userType)// 存储用户类型
                .issuedAt(new Date(now))// 存储令牌生成时间
                .expiration(expiration)// 过期时间
                .signWith(secretKey())// 签名令牌
                .compact();// 生成令牌
    }

    /**
     * 解析令牌，非法或过期时抛出 401 业务异常
     * 服务器对每个 请求 重新验明正身的动作
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()// 解析令牌
                    .verifyWith(secretKey())// 验证令牌签名
                    .build()// 构建解析器
                    .parseSignedClaims(token)// 解析令牌
                    .getPayload();// 获取令牌有效载荷
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("令牌解析失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    // 获取令牌过期时间（秒）
    public long getExpireSeconds() {
        return jwtProperties.getExpireMinutes() * 60;
    }
}