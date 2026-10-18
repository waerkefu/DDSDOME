package com.daijia.infra;

import com.daijia.common.BusinessException;
import com.daijia.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 幂等控制。
 *
 * <p>Redis 命中则直接判定为重复请求；Redis 未启用/不可用时放行，
 * 由数据库唯一约束（如 payment.idempotency_key、orders.order_no）做最终兜底。</p>
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String KEY_PREFIX = "daijia:idem:";// 幂等令牌键前缀

    private final RedisHelper redisHelper;// Redis 帮助类

    /**
     * 获取幂等令牌。
     * @param idempotencyKey 幂等键
     * @param ttl 过期时间
     * @return true 表示首次请求，可继续处理
     */
    public boolean tryAcquire(String idempotencyKey, Duration ttl) {
        //如果幂等键为空或空白，直接返回 true
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return true;
        }
        //否则尝试设置 Redis 键值对
        return redisHelper.setIfAbsent(KEY_PREFIX + idempotencyKey, "1", ttl);
    }

    /**
     * 获取幂等令牌，若为重复请求则抛出业务异常。
     * @param idempotencyKey 幂等键
     * @param ttl 过期时间
     * @throws BusinessException 若为重复请求
     */
    public void acquireOrThrow(String idempotencyKey, Duration ttl) {
        //如果获取失败，抛出业务异常
        if (!tryAcquire(idempotencyKey, ttl)) {
            throw new BusinessException(ResultCode.DUPLICATE_REQUEST);
        }
    }

    /**
     * 释放幂等令牌。
     * @param idempotencyKey 幂等键
     * @return true 表示成功释放令牌
     */
    public void release(String idempotencyKey) {
        //如果幂等键为空或空白，直接返回
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        redisHelper.delete(KEY_PREFIX + idempotencyKey);
    }
}
