package com.flashsale.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付回调消息DTO
 * @author 21311
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 第三方支付流水号
     */
    private String thirdPartyPaymentNo;

    /**
     * 支付状态：0-待支付，1-支付成功，2-支付失败，3-已退款
     */
    private Integer paymentStatus;

    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付方式：1-支付宝，2-微信，3-银联
     */
    private Integer paymentType;

    /**
     * 支付时间戳
     */
    private Long paymentTime;

    /**
     * 回调时间戳
     */
    private Long callbackTime;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 备注信息
     */
    private String remark;

    public PaymentCallbackMessage(String orderNo, String paymentNo, Integer paymentStatus, BigDecimal paymentAmount) {
        this.orderNo = orderNo;
        this.paymentNo = paymentNo;
        this.paymentStatus = paymentStatus;
        this.paymentAmount = paymentAmount;
        this.callbackTime = System.currentTimeMillis();
    }
} 