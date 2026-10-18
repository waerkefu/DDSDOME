package com.daijia.order.service;

import com.daijia.common.BusinessException;
import com.daijia.common.ResultCode;
import com.daijia.order.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态机。
 *
 * <p>所有订单状态流转必须经过本类校验，未在流转表中声明的变更一律拒绝，
 * 从源头避免出现非法状态与脏数据。</p>
 *
 * <pre>
 * PENDING_ACCEPT(待接受) ──► ACCEPTED(已接受) ──► IN_SERVICE(服务中) ──► PENDING_PAYMENT(待支付) ──► COMPLETED(已完成)
 *       │                       │                                         │
 *       ├──► CANCELLED(已取消)   ├──► CANCELLED(已取消)                     └──► CANCELLED(已取消)
 *       └──► CLOSED(已关闭)
 * </pre>
 */
@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS;

    // 初始化状态流转表
    static {
        //`EnumMap` 是 JDK 专门为「枚举做 key」设计的 Map
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        // 待接受
        transitions.put(OrderStatus.PENDING_ACCEPT,
                //`of` 是`EnumSet` 类的 静态工厂方法 ，作用是：把括号里列举的几个元素装进一个 Set 里，返回这个集合
                //                      已接受                已取消                  已关闭
                EnumSet.of(OrderStatus.ACCEPTED, OrderStatus.CANCELLED, OrderStatus.CLOSED));

        // 已接受
        transitions.put(OrderStatus.ACCEPTED,
                //                      服务中                已取消
                EnumSet.of(OrderStatus.IN_SERVICE, OrderStatus.CANCELLED));

        // 服务中
        transitions.put(OrderStatus.IN_SERVICE,
                //                      待支付
                EnumSet.of(OrderStatus.PENDING_PAYMENT));

        // 待支付
        transitions.put(OrderStatus.PENDING_PAYMENT,
                //                      已完成                已取消
                EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));

        // 终态：不可再流转
        //                            已完成             
        transitions.put(OrderStatus.COMPLETED, Collections.emptySet());
        //                            已取消             
        transitions.put(OrderStatus.CANCELLED, Collections.emptySet());
        //                           已关闭             
        transitions.put(OrderStatus.CLOSED, Collections.emptySet());
        TRANSITIONS = Collections.unmodifiableMap(transitions);// 状态流转表，不可修改
    }

    /**
     * 判断状态流转是否合法
     */
    public boolean canTransit(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return TRANSITIONS.getOrDefault(from, Collections.emptySet()).contains(to);
    }

    /**
     * 校验状态流转，非法时抛出业务异常
     */
    public void assertCanTransit(OrderStatus from, OrderStatus to) {
        if (!canTransit(from, to)) {
            throw new BusinessException(ResultCode.ORDER_STATUS_NOT_ALLOWED,
                    String.format("订单当前状态为「%s」，不允许变更为「%s」",
                            from == null ? "未知" : from.getDescription(),
                            to == null ? "未知" : to.getDescription()));
        }
    }

    /**
     * 查询某状态下允许流转到的后续状态
     */
    public Set<OrderStatus> nextStatuses(OrderStatus from) {
        return TRANSITIONS.getOrDefault(from, Collections.emptySet());
    }
}
