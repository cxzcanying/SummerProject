package com.flashsale.payment.service;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.payment.dto.PaymentDTO;

import com.flashsale.payment.vo.PaymentVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 支付服务接口
 * @author 21311
 */
public interface PaymentService {

    /**
     * 创建支付订单
     *
     * @param paymentDTO 支付信息
     * @return 支付结果
     */
    Result<PaymentVO> createPayment(PaymentDTO paymentDTO);

    /**
     * 处理支付回调
     *
     * @param paymentNo 支付流水号
     * @param thirdPartyPaymentNo 第三方支付流水号
     * @param status 支付状态
     * @return 处理结果
     */
    Result<Void> handlePaymentCallback(String paymentNo, String thirdPartyPaymentNo, Integer status);

    /**
     * 查询支付详情
     *
     * @param paymentNo 支付流水号
     * @return 支付详情
     */
    Result<PaymentVO> getPaymentDetail(String paymentNo);

    /**
     * 根据订单ID查询支付记录
     *
     * @param orderId 订单ID
     * @return 支付记录
     */
    Result<PaymentVO> getPaymentByOrderId(Long orderId);

    /**
     * 分页查询用户支付记录
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 支付记录列表
     */
    Result<PageResult<PaymentVO>> getUserPayments(Long userId, Integer page, Integer size);

    /**
     * 申请退款
     *
     * @param paymentNo 支付流水号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款结果
     */
    Result<Void> applyRefund(String paymentNo, BigDecimal refundAmount, String refundReason);

    /**
     * 处理退款回调
     *
     * @param refundNo 退款流水号
     * @param status 退款状态
     * @return 处理结果
     */
    Result<Void> handleRefundCallback(String refundNo, Integer status);

    /**
     * 取消支付
     *
     * @param paymentNo 支付流水号
     * @return 取消结果
     */
    Result<Void> cancelPayment(String paymentNo);

    /**
     * 调试方法 - 获取所有支付记录
     * 
     * @return 所有支付记录
     */
    Result<List<PaymentVO>> getAllPaymentsForDebug();
} 