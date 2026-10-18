package com.daijia.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daijia.payment.entity.Refund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RefundMapper extends BaseMapper<Refund> {

    @Select("SELECT * FROM refund WHERE idempotency_key = #{key} AND deleted = 0 LIMIT 1")
    Refund selectByIdempotencyKey(@Param("key") String key);

    /**
     * 统计订单已成功退款金额（分）
     */
    @Select("SELECT IFNULL(SUM(amount), 0) FROM refund WHERE order_id = #{orderId} AND status = 'SUCCESS' AND deleted = 0")
    Integer sumSuccessAmountByOrderId(@Param("orderId") Long orderId);
}
