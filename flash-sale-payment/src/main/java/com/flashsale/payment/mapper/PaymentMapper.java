package com.flashsale.payment.mapper;

import com.flashsale.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付Mapper接口
 * @author 21311
 */
@Mapper
public interface PaymentMapper {

    /**
     * 插入支付记录
     */
    int insert(Payment payment);

    /**
     * 根据ID查找支付记录
     */
    Payment findById(@Param("id") Long id);

    /**
     * 根据订单号查找支付记录
     */
    Payment findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据支付流水号查找支付记录
     */
    Payment findByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 根据ID更新支付记录
     */
    int updateById(Payment payment);

    /**
     * 更新支付状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
} 