package com.daijia.order.controller;

import com.daijia.common.PageQuery;
import com.daijia.common.PageResult;
import com.daijia.common.Result;
import com.daijia.order.dto.FinishServiceRequest;
import com.daijia.order.dto.OrderDetailVO;
import com.daijia.order.dto.OrderQuery;
import com.daijia.order.dto.OrderVO;
import com.daijia.order.service.OrderService;
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
 * 服务商订单接口
 */
@RestController
@RequestMapping("/api/provider/orders")
@RequiredArgsConstructor
@RequireRole("PROVIDER")
public class ProviderOrderController {

    private final OrderService orderService;

    /**
     * 接单大厅
     * @param query 查询参数
     * @return 订单分页列表
     */
    @GetMapping("/hall")
    public Result<PageResult<OrderVO>> hall(PageQuery query) {
        return Result.ok(orderService.pageHall(UserContext.currentUserId(), query));
    }

    /**
     * 抢单
     * @param orderNo 订单号
     * @return 订单VO
     */
    @PostMapping("/{orderNo}/grab")
    public Result<OrderVO> grab(@PathVariable String orderNo) {
        return Result.ok(orderService.grab(UserContext.currentUserId(), orderNo));
    }

    /**
     * 开始服务
     * @param orderNo 订单号
     * @return 订单VO
     */
    @PutMapping("/{orderNo}/start")
    public Result<OrderVO> start(@PathVariable String orderNo) {
        return Result.ok(orderService.startService(UserContext.currentUserId(), orderNo));
    }

    /**
     * 结束服务
     * @param orderNo 订单号
     * @param req 完成服务请求
     * @return 订单VO
     */
    @PutMapping("/{orderNo}/finish")
    public Result<OrderVO> finish(@PathVariable String orderNo,
                                  @Valid @RequestBody FinishServiceRequest req) {
        return Result.ok(orderService.finishService(UserContext.currentUserId(), orderNo, req));
    }

    /**
     * 查询页查询服务商订单分页列表
     * @param query 查询参数
     * @return 订单分页列表
     */
    @GetMapping
    public Result<PageResult<OrderVO>> page(OrderQuery query) {
        return Result.ok(orderService.pageForProvider(UserContext.currentUserId(), query));
    }

    /**
     * 查询服务商订单详情
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    @GetMapping("/{orderNo}")
    public Result<OrderDetailVO> detail(@PathVariable String orderNo) {
        return Result.ok(orderService.providerDetail(UserContext.currentUserId(), orderNo));
    }
}
