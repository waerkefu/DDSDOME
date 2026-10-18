package com.daijia.order.controller;

import com.daijia.common.PageResult;
import com.daijia.common.Result;
import com.daijia.order.dto.CancelOrderRequest;
import com.daijia.order.dto.OrderDetailVO;
import com.daijia.order.dto.OrderQuery;
import com.daijia.order.dto.OrderVO;
import com.daijia.order.service.OrderService;
import com.daijia.security.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端订单接口
 *
 * <p>退款接口 /api/admin/orders/{orderNo}/refund 将在 Task 9（支付模块）中补充。</p>
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@RequireRole("ADMIN")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 分页查询管理员订单分页列表
     * @param query 查询参数
     * @return 订单分页列表
     */
    @GetMapping
    public Result<PageResult<OrderVO>> page(OrderQuery query) {
        return Result.ok(orderService.pageForAdmin(query));
    }
    
    /**
     * 查询管理员订单详情
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.adminDetail(orderNo));
    }
    
    /**
     * 管理员取消订单
     * @param orderNo 订单号
     * @param req 取消订单请求
     * @return 空结果
     */
    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo,
                               @RequestBody(required = false) CancelOrderRequest req) {
        orderService.cancelByAdmin(orderNo, req == null ? null : req.getReason());
        return Result.ok();
    }
}
