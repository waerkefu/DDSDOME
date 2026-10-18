package com.daijia.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daijia.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM payment WHERE idempotency_key = #{key} AND deleted = 0 LIMIT 1")
    Payment selectByIdempotencyKey(@Param("key") String key);

    @Select("SELECT * FROM payment WHERE pay_no = #{payNo} AND deleted = 0 LIMIT 1")
    Payment selectByPayNo(@Param("payNo") String payNo);

    @Select("SELECT * FROM payment WHERE order_id = #{orderId} AND status = 'SUCCESS' AND deleted = 0 ORDER BY id DESC LIMIT 1")
    Payment selectSuccessByOrderId(@Param("orderId") Long orderId);

    /**
     * 支付成功的状态 CAS，保证回调幂等：只有处于支付中（或初始）的支付单能被置为成功。
     */
    @Update("UPDATE payment SET status = #{toStatus}, trade_no = #{tradeNo}, pay_time = NOW(), notify_time = NOW(), update_time = NOW() "
            + "WHERE id = #{paymentId} AND status IN ('INIT', 'PAYING') AND deleted = 0")
    int markPaid(@Param("paymentId") Long paymentId,
                 @Param("toStatus") String toStatus,
                 @Param("tradeNo") String tradeNo);

    @Update("UPDATE payment SET status = #{toStatus}, update_time = NOW() "
            + "WHERE id = #{paymentId} AND status = 'SUCCESS' AND deleted = 0")
    int markRefunded(@Param("paymentId") Long paymentId, @Param("toStatus") String toStatus);
}
