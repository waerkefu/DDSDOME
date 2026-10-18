package com.daijia.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.daijia.common.BusinessException;
import com.daijia.common.NoGenerator;
import com.daijia.common.PageQuery;
import com.daijia.common.PageResult;
import com.daijia.common.ResultCode;
import com.daijia.infra.IdempotencyService;
import com.daijia.infra.OrderProperties;
import com.daijia.order.dto.FinishServiceRequest;
import com.daijia.order.dto.OrderCreateRequest;
import com.daijia.order.dto.OrderDetailVO;
import com.daijia.order.dto.OrderQuery;
import com.daijia.order.dto.OrderStatusLogVO;
import com.daijia.order.dto.OrderVO;
import com.daijia.order.entity.OrderStatusLog;
import com.daijia.order.entity.Orders;
import com.daijia.order.enums.OperatorType;
import com.daijia.order.enums.OrderStatus;
import com.daijia.order.enums.OrderType;
import com.daijia.order.enums.PayStatus;
import com.daijia.order.mapper.OrderStatusLogMapper;
import com.daijia.order.mapper.OrdersMapper;
import com.daijia.order.service.OrderService;
import com.daijia.order.service.OrderStateMachine;
import com.daijia.pricing.service.PricingService;
import com.daijia.provider.entity.Provider;
import com.daijia.provider.mapper.ProviderMapper;
import com.daijia.user.entity.SysUser;
import com.daijia.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 订单服务实现。
 *
 * <p>状态流转严格遵循：查订单 → 状态机校验 → 条件更新（CAS / 抢单）→ 影响行数为 0 则抛冲突 → 写状态日志。
 * 并发正确性由数据库条件更新保证，Redis 仅用于削峰防重。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String GRAB_LOCK_PREFIX = "grab:";// 接单锁前缀
    private static final String ORDER_CREATE_PREFIX = "order:create:";// 订单创建锁前缀

    private final OrdersMapper ordersMapper;// 订单Mapper
    private final OrderStatusLogMapper orderStatusLogMapper;// 订单状态日志Mapper
    private final OrderStateMachine orderStateMachine;// 订单状态机
    private final PricingService pricingService;// 计价服务
    private final ProviderMapper providerMapper;// 服务商Mapper
    private final SysUserMapper sysUserMapper;// 用户Mapper
    private final IdempotencyService idempotencyService;// 去重服务
    private final OrderProperties orderProperties;// 订单属性

    /**
     * 创建订单
     * 
     * @param customerId 客户ID
     * @param req 订单创建请求
     * @param idempotencyKey 去重键
     * @return 订单VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(Long customerId, OrderCreateRequest req, String idempotencyKey) {
        OrderType orderType = req.getOrderType();
        validateBookingTime(orderType, req.getBookingTime());
        if (StringUtils.hasText(req.getStartAddress())
                && req.getStartAddress().equals(req.getEndAddress())) {
            throw new BusinessException(ResultCode.ORDER_PARAM_INVALID, "起点与终点地址不能相同");
        }

        if (StringUtils.hasText(idempotencyKey)) {
            idempotencyService.acquireOrThrow(
                    ORDER_CREATE_PREFIX + customerId + ":" + idempotencyKey, Duration.ofMinutes(5));
        }

        LocalDateTime now = LocalDateTime.now();
        int durationS = req.getDurationS() == null ? 0 : req.getDurationS();
        LocalDateTime serviceTime = req.getBookingTime() != null ? req.getBookingTime() : now;
        int estimateAmount = pricingService.calculateAmount(req.getCityCode(), req.getVehicleType(),
                orderType, req.getDistanceM(), durationS, serviceTime);

        Orders order = new Orders();
        order.setOrderNo(NoGenerator.orderNo());
        order.setOrderType(orderType.name());
        order.setStatus(OrderStatus.PENDING_ACCEPT.name());
        order.setCustomerId(customerId);
        order.setCityCode(req.getCityCode());
        order.setVehicleType(req.getVehicleType());
        order.setVehiclePlate(req.getVehiclePlate());
        order.setContactName(req.getContactName());
        order.setContactPhone(req.getContactPhone());
        order.setStartAddress(req.getStartAddress());
        order.setStartLongitude(req.getStartLongitude());
        order.setStartLatitude(req.getStartLatitude());
        order.setEndAddress(req.getEndAddress());
        order.setEndLongitude(req.getEndLongitude());
        order.setEndLatitude(req.getEndLatitude());
        order.setBookingTime(req.getBookingTime());
        order.setDistanceM(req.getDistanceM());
        order.setDurationS(durationS);
        order.setEstimateAmount(estimateAmount);
        order.setActualAmount(0);
        order.setPayAmount(0);
        order.setRefundAmount(0);
        order.setPayStatus(PayStatus.UNPAID.name());
        order.setRemark(req.getRemark());
        order.setVersion(0);
        order.setExpireTime(now.plusMinutes(orderProperties.getAcceptTimeoutMinutes()));
        try {
            ordersMapper.insert(order);
        } catch (DuplicateKeyException e) {
            // orders.order_no 唯一索引兜底，重复提交转为友好业务异常
            throw new BusinessException(ResultCode.DUPLICATE_REQUEST);
        }
        saveStatusLog(order, null, OrderStatus.PENDING_ACCEPT.name(),
                OperatorType.CUSTOMER, customerId, "客户下单");
        return toVO(order);
    }

    /**
     * 验证预约用车时间是否有效
     * @param orderType 订单类型
     * @param bookingTime 预约用车时间
     */
    private void validateBookingTime(OrderType orderType, LocalDateTime bookingTime) {
        boolean requireBookingTime = orderType == OrderType.SCHEDULED || orderType.isAgency();
        if (!requireBookingTime) {
            return;
        }
        if (bookingTime == null) {
            throw new BusinessException(ResultCode.BOOKING_TIME_INVALID, "预约单/代办单必须填写预约用车时间");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!bookingTime.isAfter(now)) {
            throw new BusinessException(ResultCode.BOOKING_TIME_INVALID, "预约用车时间必须晚于当前时间");
        }
        if (bookingTime.isAfter(now.plusDays(orderProperties.getMaxBookingDays()))) {
            throw new BusinessException(ResultCode.BOOKING_TIME_INVALID,
                    "预约用车时间不能超过 " + orderProperties.getMaxBookingDays() + " 天");
        }
    }

    /**
     * 查询客户订单分页列表
     * @param customerId 客户ID
     * @param query 查询参数
     * @return 订单分页列表
     */
    @Override
    public PageResult<OrderVO> pageForCustomer(Long customerId, OrderQuery query) {
        return pageQuery(query, wrapper -> wrapper.eq(Orders::getCustomerId, customerId));
    }

    /**
     * 查询客户订单详情
     * @param customerId 客户ID
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    @Override
    public OrderDetailVO customerDetail(Long customerId, String orderNo) {
        Orders order = getByOrderNoOrThrow(orderNo);
        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_TO_YOU);
        }
        return buildDetail(order);
    }

    /**
     * 客户取消订单
     * @param customerId 客户ID
     * @param orderNo 订单号
     * @param reason 取消原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByCustomer(Long customerId, String orderNo, String reason) {
        Orders order = getByOrderNoOrThrow(orderNo);
        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_TO_YOU);
        }
        cancelOrder(order, reason, OperatorType.CUSTOMER, customerId);
    }

    /**
     * 查询大厅订单分页列表
     * @param providerUserId 供应商用户ID
     * @param query 查询参数
     * @return 订单分页列表
     */
    @Override
    public PageResult<OrderVO> pageHall(Long providerUserId, PageQuery query) {
        Provider provider = requireProvider(providerUserId);
        if (!provider.canAcceptOrder()) {
            throw new BusinessException(ResultCode.PROVIDER_NOT_AVAILABLE);
        }
        Page<Orders> page = new Page<>(query.safePageNum(), query.safePageSize());
        if (!StringUtils.hasText(provider.getServiceTypes())) {
            return PageResult.empty(page.getCurrent(), page.getSize());
        }
        List<String> serviceTypes = Arrays.stream(provider.getServiceTypes().split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
        if (serviceTypes.isEmpty()) {
            return PageResult.empty(page.getCurrent(), page.getSize());
        }
        LambdaQueryWrapper<Orders> wrapper = Wrappers.<Orders>lambdaQuery()
                .eq(Orders::getStatus, OrderStatus.PENDING_ACCEPT.name())
                .isNull(Orders::getProviderId)
                .in(Orders::getOrderType, serviceTypes);
        if (StringUtils.hasText(provider.getCityCode())) {
            wrapper.and(w -> w.isNull(Orders::getCityCode).or().eq(Orders::getCityCode, provider.getCityCode()));
        }
        wrapper.orderByDesc(Orders::getCreateTime).orderByDesc(Orders::getId);
        Page<Orders> result = ordersMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 服务商接单
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @return 接单结果VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO grab(Long providerUserId, String orderNo) {
        Provider provider = requireProvider(providerUserId);
        if (!provider.canAcceptOrder()) {
            throw new BusinessException(ResultCode.PROVIDER_NOT_AVAILABLE);
        }
        String lockKey = GRAB_LOCK_PREFIX + orderNo;
        // Redis 仅做短时防重，正确性由 grabOrder 条件更新保证
        if (!idempotencyService.tryAcquire(lockKey, Duration.ofSeconds(5))) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_ACCEPTED);
        }
        try {
            Orders order = getByOrderNoOrThrow(orderNo);
            OrderStatus current = requireStatus(order);
            if (current != OrderStatus.PENDING_ACCEPT) {
                throw new BusinessException(ResultCode.ORDER_ALREADY_ACCEPTED);
            }
            orderStateMachine.assertCanTransit(current, OrderStatus.ACCEPTED);
            if (!canServe(provider, order)) {
                throw new BusinessException(ResultCode.ORDER_STATUS_NOT_ALLOWED, "该订单不在服务商可服务范围内");
            }
            LocalDateTime acceptTime = LocalDateTime.now();
            int rows = ordersMapper.grabOrder(order.getId(), provider.getId(), acceptTime,
                    current.name(), OrderStatus.ACCEPTED.name());
            if (rows == 0) {
                throw new BusinessException(ResultCode.ORDER_ALREADY_ACCEPTED);
            }
            order.setStatus(OrderStatus.ACCEPTED.name());
            order.setProviderId(provider.getId());
            order.setAcceptTime(acceptTime);
            saveStatusLog(order, current.name(), OrderStatus.ACCEPTED.name(),
                    OperatorType.PROVIDER, providerUserId, "服务商接单");
            return toVO(order);
        } finally {
            idempotencyService.release(lockKey);
        }
    }

    /**
     * 服务商开始服务
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @return 开始服务结果VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO startService(Long providerUserId, String orderNo) {
        Provider provider = requireProvider(providerUserId);
        Orders order = getByOrderNoOrThrow(orderNo);
        assertProviderOwns(provider, order);
        OrderStatus current = requireStatus(order);
        orderStateMachine.assertCanTransit(current, OrderStatus.IN_SERVICE);
        int rows = ordersMapper.casStatus(order.getId(), current.name(), OrderStatus.IN_SERVICE.name());
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_VERSION_CONFLICT);
        }
        LocalDateTime now = LocalDateTime.now();
        ordersMapper.update(null, Wrappers.<Orders>lambdaUpdate()
                .eq(Orders::getId, order.getId())
                .set(Orders::getServiceStartTime, now));
        order.setStatus(OrderStatus.IN_SERVICE.name());
        order.setServiceStartTime(now);
        saveStatusLog(order, current.name(), OrderStatus.IN_SERVICE.name(),
                OperatorType.PROVIDER, providerUserId, "开始服务");
        return toVO(order);
    }

    /**
     * 服务商结束服务
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @param req 结束服务请求
     * @return 结束服务结果VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO finishService(Long providerUserId, String orderNo, FinishServiceRequest req) {
        Provider provider = requireProvider(providerUserId);
        Orders order = getByOrderNoOrThrow(orderNo);
        assertProviderOwns(provider, order);
        OrderStatus current = requireStatus(order);
        orderStateMachine.assertCanTransit(current, OrderStatus.PENDING_PAYMENT);

        LocalDateTime now = LocalDateTime.now();
        int actualAmount = pricingService.calculateAmount(order.getCityCode(), order.getVehicleType(),
                parseOrderType(order.getOrderType()), req.getDistanceM(), req.getDurationS(), now);

        int rows = ordersMapper.casStatus(order.getId(), current.name(), OrderStatus.PENDING_PAYMENT.name());
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_VERSION_CONFLICT);
        }
        ordersMapper.update(null, Wrappers.<Orders>lambdaUpdate()
                .eq(Orders::getId, order.getId())
                .set(Orders::getServiceEndTime, now)
                .set(Orders::getDistanceM, req.getDistanceM())
                .set(Orders::getDurationS, req.getDurationS())
                .set(Orders::getActualAmount, actualAmount));

        order.setStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setServiceEndTime(now);
        order.setDistanceM(req.getDistanceM());
        order.setDurationS(req.getDurationS());
        order.setActualAmount(actualAmount);
        saveStatusLog(order, current.name(), OrderStatus.PENDING_PAYMENT.name(),
                OperatorType.PROVIDER, providerUserId, "结束服务，进入待支付");
        return toVO(order);
    }

    /**
     * 查询服务商订单分页列表
     * @param providerUserId 供应商用户ID
     * @param query 查询参数
     * @return 订单分页列表
     */
    @Override
    public PageResult<OrderVO> pageForProvider(Long providerUserId, OrderQuery query) {
        Provider provider = requireProvider(providerUserId);
        return pageQuery(query, wrapper -> wrapper.eq(Orders::getProviderId, provider.getId()));
    }

    /**
     * 查询服务商订单详情
     * @param providerUserId 供应商用户ID
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    @Override
    public OrderDetailVO providerDetail(Long providerUserId, String orderNo) {
        Provider provider = requireProvider(providerUserId);
        Orders order = getByOrderNoOrThrow(orderNo);
        assertProviderOwns(provider, order);
        return buildDetail(order);
    }

    /**
     * 分页查询管理员订单分页列表
     * @param query 查询参数
     * @return 订单分页列表
     */
    @Override
    public PageResult<OrderVO> pageForAdmin(OrderQuery query) {
        return pageQuery(query, null);
    }

    /**
     * 查询管理员订单详情
     * @param orderNo 订单号
     * @return 订单详情VO
     */
    @Override
    public OrderDetailVO adminDetail(String orderNo) {
        return buildDetail(getByOrderNoOrThrow(orderNo));
    }

    /**
     * 管理员取消订单
     * @param orderNo 订单号
     * @param reason 取消原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelByAdmin(String orderNo, String reason) {
        Orders order = getByOrderNoOrThrow(orderNo);
        cancelOrder(order, reason, OperatorType.ADMIN, null);
    }

    /**
     * 根据订单号查询订单实体
     * @param orderNo 订单号
     * @return 订单实体
     */
    @Override
    public Orders getByOrderNoOrThrow(String orderNo) {
        Orders order = ordersMapper.selectOne(Wrappers.<Orders>lambdaQuery()
                .eq(Orders::getOrderNo, orderNo)
                .last("LIMIT 1"));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 批量关闭超时未接单的订单
     * @return 影响的订单数
     */
    @Override
    public int closeExpiredOrders() {
        return ordersMapper.closeExpiredOrders(OrderStatus.PENDING_ACCEPT.name(),
                OrderStatus.CLOSED.name(), "超时未接单自动关闭");
    }

    // ------------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------------

    /**
     * 取消订单
     * @param order 订单实体
     * @param reason 取消原因
     * @param operatorType 操作类型
     * @param operatorId 操作人ID
     */
    private void cancelOrder(Orders order, String reason, OperatorType operatorType, Long operatorId) {
        OrderStatus current = requireStatus(order);
        if (current != OrderStatus.PENDING_ACCEPT
                && current != OrderStatus.ACCEPTED
                && current != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ResultCode.ORDER_STATUS_NOT_ALLOWED);
        }
        orderStateMachine.assertCanTransit(current, OrderStatus.CANCELLED);
        int rows = ordersMapper.casStatus(order.getId(), current.name(), OrderStatus.CANCELLED.name());
        if (rows == 0) {
            throw new BusinessException(ResultCode.ORDER_VERSION_CONFLICT);
        }
        ordersMapper.update(null, Wrappers.<Orders>lambdaUpdate()
                .eq(Orders::getId, order.getId())
                .set(Orders::getCancelTime, LocalDateTime.now())
                .set(Orders::getCancelReason, reason));
        order.setStatus(OrderStatus.CANCELLED.name());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
        saveStatusLog(order, current.name(), OrderStatus.CANCELLED.name(),
                operatorType, operatorId, StringUtils.hasText(reason) ? reason : "订单取消");
    }

    /**
     * 分页查询订单
     * @param query 查询参数
     * @param customizer 自定义查询条件
     * @return 订单分页列表
     */
    private PageResult<OrderVO> pageQuery(OrderQuery query, Consumer<LambdaQueryWrapper<Orders>> customizer) {
        LambdaQueryWrapper<Orders> wrapper = Wrappers.lambdaQuery(Orders.class);
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Orders::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getOrderType())) {
            wrapper.eq(Orders::getOrderType, query.getOrderType());
        }
        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.eq(Orders::getOrderNo, query.getOrderNo());
        }
        if (query.getCustomerId() != null) {
            wrapper.eq(Orders::getCustomerId, query.getCustomerId());
        }
        if (query.getProviderId() != null) {
            wrapper.eq(Orders::getProviderId, query.getProviderId());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(Orders::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Orders::getCreateTime, query.getEndTime());
        }
        if (customizer != null) {
            customizer.accept(wrapper);
        }
        wrapper.orderByDesc(Orders::getCreateTime).orderByDesc(Orders::getId);

        Page<Orders> page = new Page<>(query.safePageNum(), query.safePageSize());
        Page<Orders> result = ordersMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 构建订单详情VO
     * @param order 订单实体
     * @return 订单详情VO
     */
    private OrderDetailVO buildDetail(Orders order) {
        OrderDetailVO detail = new OrderDetailVO();
        detail.setOrder(toVO(order));
        List<OrderStatusLog> logs = orderStatusLogMapper.selectList(Wrappers.<OrderStatusLog>lambdaQuery()
                .eq(OrderStatusLog::getOrderId, order.getId())
                .orderByAsc(OrderStatusLog::getId));
        detail.setStatusLogs(logs.stream().map(this::toLogVO).toList());
        return detail;
    }

    /**
     * 保存订单状态变更日志
     * @param order 订单实体
     * @param fromStatus 从状态
     * @param toStatus 到状态
     * @param operatorType 操作类型
     * @param operatorId 操作人ID
     * @param remark 备注
     */
    private void saveStatusLog(Orders order, String fromStatus, String toStatus,
                               OperatorType operatorType, Long operatorId, String remark) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setOperatorType(operatorType.getCode());
        log.setOperatorName(resolveOperatorName(operatorType, operatorId));
        log.setRemark(remark);
        orderStatusLogMapper.insert(log);
    }

    /**
     * 解析操作人名称
     * @param operatorType 操作类型
     * @param operatorId 操作人ID
     * @return 操作人名称
     */
    private String resolveOperatorName(OperatorType operatorType, Long operatorId) {
        if (operatorId == null || operatorType == OperatorType.SYSTEM) {
            return operatorType.getDescription();
        }
        SysUser user = sysUserMapper.selectById(operatorId);
        if (user == null) {
            return operatorType.getDescription();
        }
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    /**
     * 校验服务商是否存在
     * @param providerUserId 供应商用户ID
     * @return 服务商实体
     */
    private Provider requireProvider(Long providerUserId) {
        Provider provider = providerMapper.selectByUserId(providerUserId);
        if (provider == null) {
            throw new BusinessException(ResultCode.PROVIDER_NOT_FOUND);
        }
        return provider;
    }

    /**
     * 校验服务商是否为订单所有者
     * @param provider 服务商实体
     * @param order 订单实体
     */
    private void assertProviderOwns(Provider provider, Orders order) {
        if (order.getProviderId() == null || !order.getProviderId().equals(provider.getId())) {
            throw new BusinessException(ResultCode.ORDER_NOT_BELONG_TO_YOU);
        }
    }

    /**
     * 服务商是否可服务该订单：服务类型匹配，且城市为空或与订单一致
     * @param provider 服务商实体
     * @param order 订单实体
     * @return 是否可服务
     */
    private boolean canServe(Provider provider, Orders order) {
        if (!StringUtils.hasText(provider.getServiceTypes())) {
            return false;
        }
        boolean typeMatched = Arrays.stream(provider.getServiceTypes().split(","))
                .map(String::trim)
                .anyMatch(type -> type.equals(order.getOrderType()));
        if (!typeMatched) {
            return false;
        }
        return !StringUtils.hasText(provider.getCityCode())
                || !StringUtils.hasText(order.getCityCode())
                || provider.getCityCode().equals(order.getCityCode());
    }

    /**
     * 校验订单状态是否允许变更
     * @param order 订单实体
     * @return 订单状态
     */
    private OrderStatus requireStatus(Orders order) {
        OrderStatus status = OrderStatus.of(order.getStatus());
        if (status == null) {
            throw new BusinessException(ResultCode.ORDER_STATUS_NOT_ALLOWED);
        }
        return status;
    }

    /**
     * 解析订单类型
     * @param name 订单类型名称
     * @return 订单类型
     */
    private OrderType parseOrderType(String name) {
        if (name == null) {
            return null;
        }
        for (OrderType type : OrderType.values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 解析支付状态
     * @param name 支付状态名称
     * @return 支付状态
     */
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

    /**
     * 将订单实体转换为VO
     * @param order 订单实体
     * @return 订单VO
     */
    private OrderVO toVO(Orders order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderType(order.getOrderType());
        OrderType orderType = parseOrderType(order.getOrderType());
        vo.setOrderTypeDesc(orderType == null ? order.getOrderType() : orderType.getDescription());
        vo.setStatus(order.getStatus());
        OrderStatus status = OrderStatus.of(order.getStatus());
        vo.setStatusDesc(status == null ? order.getStatus() : status.getDescription());
        vo.setCustomerId(order.getCustomerId());
        vo.setProviderId(order.getProviderId());
        vo.setCityCode(order.getCityCode());
        vo.setVehicleType(order.getVehicleType());
        vo.setVehiclePlate(order.getVehiclePlate());
        vo.setContactName(order.getContactName());
        vo.setContactPhone(order.getContactPhone());
        vo.setStartAddress(order.getStartAddress());
        vo.setEndAddress(order.getEndAddress());
        vo.setBookingTime(order.getBookingTime());
        vo.setAcceptTime(order.getAcceptTime());
        vo.setServiceStartTime(order.getServiceStartTime());
        vo.setServiceEndTime(order.getServiceEndTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setCancelTime(order.getCancelTime());
        vo.setCancelReason(order.getCancelReason());
        vo.setDistanceM(order.getDistanceM());
        vo.setDurationS(order.getDurationS());
        vo.setEstimateAmount(order.getEstimateAmount());
        vo.setActualAmount(order.getActualAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setRefundAmount(order.getRefundAmount());
        vo.setPayStatus(order.getPayStatus());
        PayStatus payStatus = parsePayStatus(order.getPayStatus());
        vo.setPayStatusDesc(payStatus == null ? order.getPayStatus() : payStatus.getDescription());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setCustomerName(resolveCustomerName(order.getCustomerId()));
        vo.setProviderName(resolveProviderName(order.getProviderId()));
        return vo;
    }

    /**
     * 根据客户ID解析客户昵称或用户名
     * @param customerId 客户ID
     * @return 客户昵称或用户名
     */
    private String resolveCustomerName(Long customerId) {
        if (customerId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(customerId);
        if (user == null) {
            return null;
        }
        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    /**
     * 根据服务商ID解析服务商真实姓名
     * @param providerId 服务商ID
     * @return 服务商真实姓名
     */
    private String resolveProviderName(Long providerId) {
        if (providerId == null) {
            return null;
        }
        Provider provider = providerMapper.selectById(providerId);
        return provider == null ? null : provider.getRealName();
    }

    /**
     * 将订单状态变更志实体转换为VO
     * @param log 订单状态变更志实体
     * @return 订单状态变更志VO
     */
    private OrderStatusLogVO toLogVO(OrderStatusLog log) {
        OrderStatusLogVO vo = new OrderStatusLogVO();
        vo.setFromStatus(log.getFromStatus());
        vo.setToStatus(log.getToStatus());
        OrderStatus from = OrderStatus.of(log.getFromStatus());
        OrderStatus to = OrderStatus.of(log.getToStatus());
        vo.setFromStatusDesc(from == null ? log.getFromStatus() : from.getDescription());
        vo.setToStatusDesc(to == null ? log.getToStatus() : to.getDescription());
        vo.setOperatorType(log.getOperatorType());
        vo.setOperatorName(log.getOperatorName());
        vo.setRemark(log.getRemark());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }
}
