package com.daijia.payment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.daijia.common.BusinessException;
import com.daijia.common.NoGenerator;
import com.daijia.common.ResultCode;
import com.daijia.infra.IdempotencyService;
import com.daijia.order.entity.OrderStatusLog;
import com.daijia.order.entity.Orders;
import com.daijia.order.enums.OperatorType;
import com.daijia.order.enums.OrderStatus;
import com.daijia.order.enums.PayStatus;
import com.daijia.order.mapper.OrderStatusLogMapper;
import com.daijia.order.mapper.OrdersMapper;
import com.daijia.order.service.OrderService;
import com.daijia.order.service.OrderStateMachine;
import com.daijia.payment.dto.PaymentCallbackRequest;
import com.daijia.payment.dto.PaymentCreateVO;
import com.daijia.payment.dto.PaymentVO;
import com.daijia.payment.dto.RefundRequest;
import com.daijia.payment.dto.RefundVO;
import com.daijia.payment.entity.Payment;
import com.daijia.payment.entity.Refund;
import com.daijia.payment.enums.PaymentChannel;
import com.daijia.payment.enums.PaymentStatus;
import com.daijia.payment.enums.RefundStatus;
import com.daijia.payment.mapper.PaymentMapper;
import com.daijia.payment.mapper.RefundMapper;
import com.daijia.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付服务实现。
 *
 * <p>幂等设计：创建支付单依靠「幂等键唯一索引 + 先查后插」；回调依靠 {@code PaymentMapper.markPaid}
 * 的状态 CAS，重复回调影响行数为 0 时直接返回，不抛异常。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String MOCK_PAY_URL = "/api/public/payment/mock-callback";
    private static final int PAY_EXPIRE_SECONDS = 300;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final String PAYMENT_CREATE_PREFIX = "payment:create:";
    private static final String CALLBACK_PREFIX = "pay:callback:";

    private final PaymentMapper paymentMapper;
    private final RefundMapper refundMapper;
    private final OrdersMapper ordersMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderStateMachine orderStateMachine;
    private final IdempotencyService idempotencyService;
    private final OrderService orderService;

    /**
     * 创建支付单
     * 
     * @param customerId 客户ID
     * @param orderNo    订单号
     * @param idempotencyKey 幂等键，为空时以订单号兜底
     * @return 支付单结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentCreateVO createPayment(Long customerId, String orderNo, String idempotencyKey) {
        return createPaymentInternal(customerId, orderNo, idempotencyKey);
    }

    /**
     * 创建支付单（管理员代客操作）
     * 
     * @param orderNo    订单号
     * @param idempotencyKey 幂等键，为空时以订单号兜底
     * @return 支付单结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentCreateVO createPaymentForAdmin(String orderNo, String idempotencyKey) {
        return createPaymentInternal(null, orderNo, idempotencyKey);
    }

    /**
     * 创建支付单（内部方法）
     * 
     * @param customerId 客户ID
     * @param orderNo    订单号
     * @param idempotencyKey 幂等键，为空时以订单号兜底
     * @return 支付单结果
     */
    private PaymentCreateVO createPaymentInternal(Long customerId, String orderNo, String idempotencyKey) {
        Orders order = orderService.getByOrderNoOrThrow(orderNo);
        // customerId 为空表示管理员代客操作，跳过归属校验
        if (customerId != null && !customerId.equals(order.getCustomerId())) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_TO_YOU);
        }
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_NOT_ALLOWED);
        }
        if (paymentMapper.selectSuccessByOrderId(order.getId()) != null) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_NOT_ALLOWED, "该订单已支付，请勿重复支付");
        }

        // 幂等键为空时以订单号兜底
        String key = StringUtils.hasText(idempotencyKey) ? idempotencyKey : orderNo;
        Payment existing = paymentMapper.selectByIdempotencyKey(key);
        if (existing != null) {
            return toCreateVO(existing);
        }
        idempotencyService.acquireOrThrow(PAYMENT_CREATE_PREFIX + key, Duration.ofSeconds(10));

        Payment payment = new Payment();
        payment.setPayNo(NoGenerator.payNo());
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setCustomerId(order.getCustomerId());
        payment.setAmount(intValue(order.getActualAmount()));
        payment.setChannel(PaymentChannel.MOCK.name());
        payment.setStatus(PaymentStatus.PAYING.name());
        payment.setIdempotencyKey(key);
        try {
            paymentMapper.insert(payment);
        } catch (DuplicateKeyException e) {
            // 幂等键唯一索引兜底：并发重复请求返回已存在支付单
            Payment duplicated = paymentMapper.selectByIdempotencyKey(key);
            if (duplicated != null) {
                return toCreateVO(duplicated);
            }
            throw new BusinessException(ResultCode.DUPLICATE_REQUEST);
        }

        updateOrderPayStatus(order.getId(), PayStatus.PAYING.name());
        return toCreateVO(payment);
    }

    /**
     * 处理支付网关回调
     * 
     * @param req 支付网关回调请求
     * @return 无
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(PaymentCallbackRequest req) {
        Payment payment = paymentMapper.selectByPayNo(req.getPayNo());
        // 判断支付单是否存在
        if (payment == null) {
            //不存在支付单，抛出异常
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }

        //判断支付结果是否成功
        if (!Boolean.TRUE.equals(req.getSuccess())) {
            markPaymentFailed(payment.getId());
            Orders order = ordersMapper.selectById(payment.getOrderId());
            // 判断订单是否存在且支付状态为 PAYING，如果是则更新为 UNPAID
            if (order != null && PayStatus.PAYING != null && PayStatus.PAYING.name().equals(order.getPayStatus())) {
                updateOrderPayStatus(order.getId(), PayStatus.UNPAID.name());
            }
            return;
        }

        // Redis 幂等加速：重复回调直接返回；Redis 不可用时降级放行，由状态 CAS 兜底
        if (!idempotencyService.tryAcquire(CALLBACK_PREFIX + req.getPayNo(), Duration.ofSeconds(30))) {
            return;
        }

        // 交易流水号为空时以支付单号前缀 MOCK 模拟
        String tradeNo = StringUtils.hasText(req.getTradeNo()) ? req.getTradeNo() : "MOCK" + req.getPayNo();
        int rows = paymentMapper.markPaid(payment.getId(), PaymentStatus.SUCCESS.name(), tradeNo);
        if (rows == 0) {
            // 已处理过（非 INIT/PAYING），幂等返回
            return;
        }

        Orders order = orderService.getByOrderNoOrThrow(payment.getOrderNo());
        OrderStatus current = OrderStatus.of(order.getStatus());
        orderStateMachine.assertCanTransit(current, OrderStatus.COMPLETED);

        int paidAmount = intValue(payment.getAmount());
        ordersMapper.update(null, Wrappers.<Orders>lambdaUpdate()
                .eq(Orders::getId, order.getId())
                .set(Orders::getPayStatus, PayStatus.PAID.name())
                .set(Orders::getPayAmount, paidAmount)
                .set(Orders::getFinishTime, LocalDateTime.now()));
        int casRows = ordersMapper.casStatus(order.getId(), current.name(), OrderStatus.COMPLETED.name());
        if (casRows == 0) {
            throw new BusinessException(ResultCode.ORDER_VERSION_CONFLICT);
        }

        order.setStatus(OrderStatus.COMPLETED.name());
        order.setPayStatus(PayStatus.PAID.name());
        order.setPayAmount(paidAmount);
        order.setFinishTime(LocalDateTime.now());
        saveStatusLog(order, current.name(), OrderStatus.COMPLETED.name(),
                OperatorType.CUSTOMER, order.getCustomerId(), "模拟支付成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundVO refund(String orderNo, RefundRequest req, Long operatorId) {
        Orders order = orderService.getByOrderNoOrThrow(orderNo);
        PayStatus payStatus = parsePayStatus(order.getPayStatus());
        if (payStatus != PayStatus.PAID && payStatus != PayStatus.PARTIAL_REFUNDED) {
            throw new BusinessException(ResultCode.PAYMENT_STATUS_NOT_ALLOWED, "订单未支付或不可退款");
        }

        Payment payment = paymentMapper.selectSuccessByOrderId(order.getId());
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }

        int paidAmount = intValue(order.getPayAmount());
        int actualAmount = intValue(order.getActualAmount());
        Integer refundedSum = refundMapper.sumSuccessAmountByOrderId(order.getId());
        int refundedAmount = refundedSum == null ? 0 : refundedSum;
        int requestAmount = intValue(req.getAmount());

        if (requestAmount <= 0) {
            throw new BusinessException(ResultCode.REFUND_AMOUNT_EXCEED, "退款金额必须大于 0");
        }
        // 金额守恒：已退款 + 本次 ≤ 已支付 ≤ 订单实际金额
        if (paidAmount > actualAmount || refundedAmount + requestAmount > paidAmount) {
            throw new BusinessException(ResultCode.REFUND_AMOUNT_EXCEED);
        }

        String refundNo = NoGenerator.refundNo();
        String idempotencyKey = "refund:" + orderNo + ":" + refundNo;
        Refund refund = new Refund();
        refund.setRefundNo(refundNo);
        refund.setPaymentId(payment.getId());
        refund.setPayNo(payment.getPayNo());
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setAmount(requestAmount);
        refund.setReason(req.getReason());
        refund.setStatus(RefundStatus.SUCCESS.name());
        refund.setIdempotencyKey(idempotencyKey);
        refund.setRefundTime(LocalDateTime.now());
        refund.setOperatorId(operatorId);
        try {
            refundMapper.insert(refund);
        } catch (DuplicateKeyException e) {
            Refund duplicated = refundMapper.selectByIdempotencyKey(idempotencyKey);
            if (duplicated != null) {
                return toRefundVO(duplicated);
            }
            throw new BusinessException(ResultCode.DUPLICATE_REQUEST);
        }

        int newRefundedAmount = refundedAmount + requestAmount;
        PayStatus newPayStatus = newRefundedAmount >= paidAmount
                ? PayStatus.REFUNDED : PayStatus.PARTIAL_REFUNDED;
        ordersMapper.update(null, Wrappers.<Orders>lambdaUpdate()
                .eq(Orders::getId, order.getId())
                .set(Orders::getRefundAmount, newRefundedAmount)
                .set(Orders::getPayStatus, newPayStatus.name()));
        if (newPayStatus == PayStatus.REFUNDED) {
            paymentMapper.markRefunded(payment.getId(), PaymentStatus.REFUNDED.name());
        }
        return toRefundVO(refund);
    }

    @Override
    public PaymentVO getByPayNo(String payNo) {
        Payment payment = paymentMapper.selectByPayNo(payNo);
        if (payment == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        return toVO(payment);
    }

    @Override
    public PaymentVO getByOrderNo(String orderNo) {
        List<Payment> payments = paymentMapper.selectList(Wrappers.<Payment>lambdaQuery()
                .eq(Payment::getOrderNo, orderNo)
                .orderByDesc(Payment::getId)
                .last("LIMIT 1"));
        if (payments.isEmpty()) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        return toVO(payments.get(0));
    }

    // ------------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------------

    private void updateOrderPayStatus(Long orderId, String payStatus) {
        ordersMapper.update(null, Wrappers.<Orders>lambdaUpdate()
                .eq(Orders::getId, orderId)
                .set(Orders::getPayStatus, payStatus));
    }

    private void markPaymentFailed(Long paymentId) {
        paymentMapper.update(null, Wrappers.<Payment>lambdaUpdate()
                .eq(Payment::getId, paymentId)
                .in(Payment::getStatus, PaymentStatus.INIT.name(), PaymentStatus.PAYING.name())
                .set(Payment::getStatus, PaymentStatus.FAILED.name()));
    }

    private void saveStatusLog(Orders order, String fromStatus, String toStatus,
                               OperatorType operatorType, Long operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorType(operatorType.getCode());
        log.setOperatorName(operatorType.getDescription());
        log.setRemark(remark);
        orderStatusLogMapper.insert(log);
    }

    private PaymentCreateVO toCreateVO(Payment payment) {
        PaymentCreateVO vo = new PaymentCreateVO();
        vo.setPayNo(payment.getPayNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setAmount(payment.getAmount());
        vo.setAmountYuan(toYuan(payment.getAmount()));
        vo.setChannel(payment.getChannel());
        vo.setStatus(payment.getStatus());
        vo.setMockPayUrl(MOCK_PAY_URL);
        vo.setExpireSeconds(PAY_EXPIRE_SECONDS);
        return vo;
    }

    private PaymentVO toVO(Payment payment) {
        PaymentVO vo = new PaymentVO();
        vo.setId(payment.getId());
        vo.setPayNo(payment.getPayNo());
        vo.setOrderNo(payment.getOrderNo());
        vo.setAmount(payment.getAmount());
        vo.setAmountYuan(toYuan(payment.getAmount()));
        vo.setChannel(payment.getChannel());
        vo.setStatus(payment.getStatus());
        vo.setPayTime(payment.getPayTime());
        vo.setCreateTime(payment.getCreateTime());
        return vo;
    }

    private RefundVO toRefundVO(Refund refund) {
        RefundVO vo = new RefundVO();
        vo.setId(refund.getId());
        vo.setRefundNo(refund.getRefundNo());
        vo.setOrderNo(refund.getOrderNo());
        vo.setPayNo(refund.getPayNo());
        vo.setAmount(refund.getAmount());
        vo.setAmountYuan(toYuan(refund.getAmount()));
        vo.setStatus(refund.getStatus());
        vo.setReason(refund.getReason());
        vo.setRefundTime(refund.getRefundTime());
        vo.setCreateTime(refund.getCreateTime());
        return vo;
    }

    private PayStatus parsePayStatus(String name) {
        if (name == null) {
            return null;
        }
        for (PayStatus status : PayStatus.values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }
        return null;
    }

    private int intValue(Integer value) {
        return value == null ? 0 : value;
    }

    private String toYuan(Integer fen) {
        if (fen == null) {
            return null;
        }
        return BigDecimal.valueOf(fen).divide(HUNDRED, 2, RoundingMode.HALF_UP).toPlainString();
    }
}
