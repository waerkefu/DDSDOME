package com.daijia.order.task;

import com.daijia.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 待接单订单超时自动关闭任务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)//每60秒执行一次
    public void closeExpiredOrders() {
        try {
            int count = orderService.closeExpiredOrders();
            if (count > 0) {
                log.info("已自动关闭超时未接单订单 {} 笔", count);
            }
        } catch (Exception e) {
            log.error("关闭超时未接单订单任务执行失败", e);
        }
    }
}
