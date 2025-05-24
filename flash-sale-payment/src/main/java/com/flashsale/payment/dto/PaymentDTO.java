package com.flashsale.payment.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付DTO
 * @author 21311
 */
@Data
public class PaymentDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;

    /**
     * 支付方式：1-支付宝，2-微信，3-银行卡
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /**
     * 备注
     */
    private String remark;
} 