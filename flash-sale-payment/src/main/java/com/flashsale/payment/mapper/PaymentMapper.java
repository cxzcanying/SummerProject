package com.flashsale.payment.mapper;

import com.flashsale.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * 根据订单ID查找支付记录
     */
    Payment findByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据支付流水号查找支付记录
     */
    Payment findByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 根据用户ID查找支付记录列表
     */
    List<Payment> findByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户支付记录
     */
    List<Payment> findByUserIdWithPage(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 统计用户支付记录总数
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据ID更新支付记录
     */
    int updateById(Payment payment);

    /**
     * 更新支付状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新支付信息
     */
    int updatePaymentInfo(@Param("id") Long id, 
                         @Param("thirdPartyPaymentNo") String thirdPartyPaymentNo,
                         @Param("status") Integer status);
} 