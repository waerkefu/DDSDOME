package com.daijia.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置（绑定 application.yml 中的 app.jwt 前缀）
 */
@Data
@Component// 注册为 Spring 组件
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** 签名密钥 */
    private String secret = "daijia-h5-platform-default-secret";
    /** 令牌有效期（分钟） */
    private long expireMinutes = 1440L;
    /** 令牌所在请求头 */
    private String header = "Authorization";
    /** 令牌前缀 */
    private String prefix = "Bearer ";
}