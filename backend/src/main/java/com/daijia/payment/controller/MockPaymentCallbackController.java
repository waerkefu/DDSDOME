package com.daijia.payment.controller;

import com.daijia.common.Result;
import com.daijia.payment.dto.PaymentCallbackRequest;
import com.daijia.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模拟第三方支付网关回调。
 *
 * <p>路径位于 /api/public/** 白名单内，无需登录。</p>
 */
@RestController
@RequestMapping("/api/public/payment")
@RequiredArgsConstructor
public class MockPaymentCallbackController {

    private final PaymentService paymentService;

    /** 模拟支付回调
     * 
     * @param req 支付回调请求
     * @return 空结果
     * @throws IllegalArgumentException 如果请求参数无效
     */
    @PostMapping("/mock-callback")
    public Result<Void> mockCallback(@Valid @RequestBody PaymentCallbackRequest req) {
        paymentService.handleCallback(req);
        return Result.ok();
    }
}
