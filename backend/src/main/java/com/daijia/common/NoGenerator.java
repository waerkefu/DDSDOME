package com.daijia.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 业务单号生成器：前缀 + 时间戳 + 随机数，长度不超过 32。
 * Service 层调用生成业务单号
 */
public final class NoGenerator {

    /** 日期时间格式化器，用于生成时间戳 */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private NoGenerator() {
    }

    /** 订单号 */
    public static String orderNo() {
        return generate("DO");
    }

    /** 支付单号 */
    public static String payNo() {
        return generate("PY");
    }

    /** 退款单号 */
    public static String refundNo() {
        return generate("RF");
    }

    /** 服务商编号 */
    public static String providerNo() {
        return generate("SP");
    }

    /** 生成业务单号 
     * @param prefix 前缀
     * @return 业务单号
     * 私有的静态方法，用于生成业务单号
     */
    private static String generate(String prefix) {
        // 生成时间戳
        String timestamp = LocalDateTime.now().format(FORMATTER);
        // 生成随机数
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        // 合并前缀、时间戳和随机数，返回业务单号
        return prefix + timestamp + random;
    }
}
