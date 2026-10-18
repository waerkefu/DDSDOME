package com.daijia.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daijia.order.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 状态 CAS 更新：仅当数据库中的当前状态与期望状态一致时才更新，返回影响行数。
     * 用于在不依赖分布式锁的前提下防止并发脏写。
     */
    @Update("UPDATE orders SET status = #{toStatus}, version = version + 1, update_time = NOW() "
            + "WHERE id = #{orderId} AND status = #{fromStatus} AND deleted = 0")
    int casStatus(@Param("orderId") Long orderId,
                  @Param("fromStatus") String fromStatus,
                  @Param("toStatus") String toStatus);

    /**
     * 抢单：仅当订单仍处于待接单状态且尚未绑定服务商时才更新成功，
     * 保证并发抢单场景下只有一个服务商能拿到订单。
     */
    @Update("UPDATE orders SET status = #{toStatus}, provider_id = #{providerId}, accept_time = #{acceptTime}, "
            + "version = version + 1, update_time = NOW() "
            + "WHERE id = #{orderId} AND status = #{fromStatus} AND provider_id IS NULL AND deleted = 0")
    int grabOrder(@Param("orderId") Long orderId,
                  @Param("providerId") Long providerId,
                  @Param("acceptTime") LocalDateTime acceptTime,
                  @Param("fromStatus") String fromStatus,
                  @Param("toStatus") String toStatus);

    /**
     * 批量关闭超时未接单的订单
     */
    @Update("UPDATE orders SET status = #{toStatus}, cancel_reason = #{reason}, version = version + 1, update_time = NOW() "
            + "WHERE status = #{fromStatus} AND expire_time IS NOT NULL AND expire_time < NOW() AND deleted = 0")
    int closeExpiredOrders(@Param("fromStatus") String fromStatus,
                           @Param("toStatus") String toStatus,
                           @Param("reason") String reason);
}
