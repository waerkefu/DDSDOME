package com.daijia.order.controller;

import com.daijia.common.PageResult;
import com.daijia.common.Result;
import com.daijia.order.dto.CancelOrderRequest;
import com.daijia.order.dto.OrderCreateRequest;
import com.daijia.order.dto.OrderDetailVO;
import com.daijia.order.dto.OrderQuery;
import com.daijia.order.dto.OrderVO;
import com.daijia.order.service.OrderService;
import com.daijia.pricing.dto.PriceEstimateRequest;
import com.daijia.pricing.dto.PriceEstimateVO;
import com.daijia.pricing.service.PricingService;
import com.daijia.security.RequireRole;
import com.daijia.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户订单接口
 */
@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@RequireRole("CUSTOMER")
public class CustomerOrderController {

    private final OrderService orderService;//订单服务
    private final PricingService pricingService;//价格服务

    /** 价格预估 
     * @param req 价格预估请求
     * @return 价格预估结果
    */
    @PostMapping("/estimate")
    public Result<PriceEstimateVO> estimate(@Valid @RequestBody PriceEstimateRequest req) {
        return Result.ok(pricingService.estimate(req));
    }

    /** 下单，支持 X-Idempotency-Key 幂等控制 
     * @param reqempotencyKey 幂等键
     * @param req 下单请求
     * @return 下单结果
    */
    @PostMapping
    public Result<OrderVO> create(
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderCreateRequest req) {
        return Result.ok(orderService.create(UserContext.currentUserId(), req, idempotencyKey));
    }

    /** 分页查询订单 
     * @param query 查询参数
     * @return 订单分页结果
    */
    @GetMapping
    public Result<PageResult<OrderVO>> page(OrderQuery query) {
        return Result.ok(orderService.pageForCustomer(UserContext.currentUserId(), query));
    }

    /** 查询订单详情 
     * @param orderNo 订单编号
     * @return 订单详情
    */
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.customerDetail(UserContext.currentUserId(), orderNo));
    }

    /** 取消订单 
     * @param orderNo 订单编号
     * @param req 取消订单请求
     * @return 取消订单结果
    */
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo,
                               @RequestBody(required = false) CancelOrderRequest req) {
        orderService.cancelByCustomer(UserContext.currentUserId(), orderNo,
                req == null ? null : req.getReason());
        return Result.ok();
    }
}
