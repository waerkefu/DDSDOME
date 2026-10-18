package com.daijia.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis 访问封装。
 *
 * <p>Redis 在此项目中只承担「加速与削峰」职责：当 Redis 未启用或不可用时，
 * 所有方法自动降级为「不做拦截」，业务正确性依然由数据库唯一约束、条件更新与乐观锁保证。
 * 这样既满足生产环境使用 Redis 的要求，又不会因为 Redis 故障导致下单、支付等核心链路不可用。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHelper {

    /** Redis 故障后的熔断时长，避免每次请求都等待连接超时 */
    private static final long CIRCUIT_BREAK_MILLIS = 30_000L;

    private final StringRedisTemplate stringRedisTemplate;// Redis 模板
    private final AppRedisProperties properties;// Redis 配置

    private volatile long circuitBreakUntil = 0L;

    /**
     * 分布式占位（SETNX）。
     * @param key 键
     * @param value 值
     * @param ttl 过期时间
     * @return true 表示获取成功；Redis 不可用时同样返回 true（降级放行，由数据库兜底）
     */
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        if (!isAvailable()) {
            return true;
        }
        try {
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, ttl);
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            markUnavailable(e);
            return true;
        }
    }

    /**
     * 设置键值对。
     * @param key 键
     * @param value 值
     * @param ttl 过期时间
     */
    public void set(String key, String value, Duration ttl) {
        if (!isAvailable()) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            markUnavailable(e);
        }
    }

    /**
     * 获取键值对。
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        if (!isAvailable()) {
            return null;
        }
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            markUnavailable(e);
            return null;
        }
    }

    /**
     * 删除键值对。
     * @param key 键
     */
    public void delete(String key) {
        if (!isAvailable()) {
            return;
        }
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            markUnavailable(e);
        }
    }

    /**
     * 判断 Redis 是否可用。
     * @return true 表示可用；false 表示不可用
     */
    public boolean isAvailable() {
        return properties.isEnabled() && System.currentTimeMillis() >= circuitBreakUntil;
    }

    /**
     * 标记 Redis 为不可用。
     * @param e 异常
     */
    private void markUnavailable(Exception e) {
        circuitBreakUntil = System.currentTimeMillis() + CIRCUIT_BREAK_MILLIS;
        log.warn("Redis 不可用，已降级为数据库一致性保障，{} ms 内不再重试。原因: {}", CIRCUIT_BREAK_MILLIS, e.getMessage());
    }
}
