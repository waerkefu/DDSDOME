package com.daijia.order.controller;

import com.daijia.common.PageResult;
import com.daijia.common.Result;
import com.daijia.order.dto.CancelOrderRequest;
import com.daijia.order.dto.OrderDetailVO;
import com.daijia.order.dto.OrderQuery;
import com.daijia.order.dto.OrderVO;
import com.daijia.order.service.OrderService;
import com.daijia.payment.dto.RefundRequest;
import com.daijia.payment.dto.RefundVO;
import com.daijia.payment.service.PaymentService;
import com.daijia.security.RequireRole;
import com.daijia.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端订单接口
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public Result<PageResult<OrderVO>> page(OrderQuery query) {
        return Result.ok(orderService.pageForAdmin(query));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.adminDetail(orderNo));
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo,
                               @RequestBody(required = false) CancelOrderRequest req) {
        orderService.cancelByAdmin(orderNo, req == null ? null : req.getReason());
        return Result.ok();
    }

    /** 退款（模拟即时到账） */
    @PostMapping("/{orderNo}/refund")
    public Result<RefundVO> refund(@PathVariable String orderNo,
                                   @Valid @RequestBody RefundRequest req) {
        return Result.ok(paymentService.refund(orderNo, req, UserContext.currentUserId()));
    }
}
