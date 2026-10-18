package com.daijia.order.service;

import com.daijia.common.PageQuery;
import com.daijia.common.PageResult;
import com.daijia.order.dto.FinishServiceRequest;
import com.daijia.order.dto.OrderCreateRequest;
import com.daijia.order.dto.OrderDetailVO;
import com.daijia.order.dto.OrderQuery;
import com.daijia.order.dto.OrderVO;
import com.daijia.order.entity.Orders;

/**
 * 订单服务。
 *
 * <p>所有状态变更均在事务内按「校验 → 条件更新 → 写日志」的顺序完成。</p>
 */
public interface OrderService {

    /**
     * 创建订单
     * @param customerId 客户ID
     * @param req 创建订单请求
     * @param idempotencyKey 幂等性键
     * @return 订单VO
     */
    OrderVO create(Long customerId, OrderCreateRequest req, String idempotencyKey);

    /**
     * 分页查询客户订单
     * @param customerId 客户ID
     * @param query 查询参数
     * @return 订单分页列表
     */
    PageResult<OrderVO> pageForCustomer(Long customerId, OrderQuery query);

    /**
     * 查询客户订单详情
     * @param customerId 客户ID
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    OrderDetailVO customerDetail(Long customerId, String orderNo);

    /**
     * 客户取消订单
     * @param customerId 客户ID
     * @param orderNo 订单号
     * @param reason 取消原因
     */
    void cancelByCustomer(Long customerId, String orderNo, String reason);

    /**
     * 分页查询服务商订单分页列表
     * @param providerUserId 供应商用户ID
     * @param query 查询参数
     * @return 订单分页列表
     */
    PageResult<OrderVO> pageHall(Long providerUserId, PageQuery query);

    /**
     * 服务商抢单
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @return 订单VO
     */
    OrderVO grab(Long providerUserId, String orderNo);

    /**
     * 服务商开始服务
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @return 订单VO
     */
    OrderVO startService(Long providerUserId, String orderNo);

    /**
     * 服务商完成服务
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @param req 完成服务请求
     * @return 订单VO
     */
    OrderVO finishService(Long providerUserId, String orderNo, FinishServiceRequest req);

    /**
     * 分页查询服务商订单分页列表
     * @param providerUserId 供应商用户ID
     * @param query 查询参数
     * @return 订单分页列表
     */
    PageResult<OrderVO> pageForProvider(Long providerUserId, OrderQuery query);

    /**
     * 查询服务商订单详情
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    OrderDetailVO providerDetail(Long providerUserId, String orderNo);

    /**
     * 分页查询管理员订单分页列表
     * @param query 查询参数
     * @return 订单分页列表
     */
    PageResult<OrderVO> pageForAdmin(OrderQuery query);

    /**
     * 查询管理员订单详情
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    OrderDetailVO adminDetail(String orderNo);

    /**
     * 管理员取消订单
     * @param orderNo 订单号
     * @param reason 取消原因
     */
    void cancelByAdmin(String orderNo, String reason);

    /**
     * 根据订单号查询订单实体
     * @param orderNo 订单号
     * @return 订单实体
     */
    Orders getByOrderNoOrThrow(String orderNo);

    // 批量关闭超时未接单的订单
    int closeExpiredOrders();
}
