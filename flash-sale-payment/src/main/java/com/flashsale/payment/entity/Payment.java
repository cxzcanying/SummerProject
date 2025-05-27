package com.flashsale.payment.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付实体类
 * @author 21311
 */
@Data
public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 支付ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 支付主题/流水号
     */
    private String paymentNo;

    /**
     * 交易ID/第三方支付流水号
     */
    private String thirdPartyPaymentNo;

    /**
     * 支付方式：1-支付宝，2-微信
     */
    private Integer paymentMethod;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态：0-待支付，1-支付成功，2-支付失败
     */
    private Integer status;

    /**
     * 回调时间/支付时间
     */
    private Date paymentTime;

    /**
     * 回调内容
     */
    private String callbackContent;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
} 