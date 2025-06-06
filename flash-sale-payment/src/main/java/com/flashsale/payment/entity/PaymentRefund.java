package com.flashsale.payment.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 支付退款实体类
 * @author 21311
 */
@Data
public class PaymentRefund implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 退款ID
     */
    private Long id;

    /**
     * 支付ID
     */
    private Long paymentId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款流水号
     */
    private String refundNo;

    /**
     * 第三方退款流水号
     */
    private String thirdPartyRefundNo;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态：0-退款中，1-退款成功，2-退款失败
     */
    private Integer status;

    /**
     * 退款时间
     */
    private Date refundTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;
} 