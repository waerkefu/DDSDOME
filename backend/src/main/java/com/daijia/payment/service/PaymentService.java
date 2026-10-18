package com.daijia.payment.service;

import com.daijia.payment.dto.PaymentCallbackRequest;
import com.daijia.payment.dto.PaymentCreateVO;
import com.daijia.payment.dto.PaymentVO;
import com.daijia.payment.dto.RefundRequest;
import com.daijia.payment.dto.RefundVO;

/**
 * 支付服务（模拟支付渠道）。
 *
 * <p>创建支付单以幂等键 + 数据库唯一索引保证幂等；回调处理以支付单状态 CAS 保证幂等。</p>
 */
public interface PaymentService {

    /**
     * 客户创建支付单
     */
    PaymentCreateVO createPayment(Long customerId, String orderNo, String idempotencyKey);

    /**
     * 管理员代客创建支付单（跳过归属校验）
     */
    PaymentCreateVO createPaymentForAdmin(String orderNo, String idempotencyKey);

    /**
     * 处理支付网关回调，幂等
     */
    void handleCallback(PaymentCallbackRequest req);

    /**
     * 退款（模拟退款即时成功）
     */
    RefundVO refund(String orderNo, RefundRequest req, Long operatorId);

    PaymentVO getByPayNo(String payNo);

    /**
     * 按订单号查询最近一笔支付单
     */
    PaymentVO getByOrderNo(String orderNo);
}
