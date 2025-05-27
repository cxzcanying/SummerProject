package com.flashsale.common.mq.message;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付处理消息
 * @author 21311
 */
@Data
@NoArgsConstructor
public class PaymentProcessMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单号
     */
    private String orderNo;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 支付方式：1-支付宝，2-微信
     */
    private Integer paymentMethod;
    
    public PaymentProcessMessage(String orderNo, Long userId, BigDecimal amount) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.amount = amount;
    }
    
    public PaymentProcessMessage(String orderNo, Long userId, BigDecimal amount, Integer paymentMethod) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
} 