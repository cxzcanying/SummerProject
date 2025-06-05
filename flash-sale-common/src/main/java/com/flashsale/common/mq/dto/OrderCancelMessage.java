package com.flashsale.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单取消消息DTO
 * @author 21311
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelMessage implements Serializable {
    @Serial
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
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀商品ID
     */
    private Long flashSaleProductId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 取消原因：1-用户主动取消，2-订单超时，3-支付失败，4-库存不足，5-系统异常
     */
    private Integer cancelReason;

    /**
     * 取消原因描述
     */
    private String cancelReasonDesc;

    /**
     * 是否需要回滚库存
     */
    private Boolean needRollbackStock;

    /**
     * 是否需要退款
     */
    private Boolean needRefund;

    /**
     * 取消时间戳
     */
    private Long cancelTime;

    /**
     * 消息ID
     */
    private String messageId;

    public OrderCancelMessage(String orderNo, Long userId, Integer cancelReason, String cancelReasonDesc) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.cancelReason = cancelReason;
        this.cancelReasonDesc = cancelReasonDesc;
        this.cancelTime = System.currentTimeMillis();
        this.needRollbackStock = true;
        // 默认需要回滚库存
        this.needRefund = false;
        // 默认不需要退款
    }

    public OrderCancelMessage(String orderNo, Long userId, Long productId, Long flashSaleProductId, 
                            Integer quantity, Integer cancelReason, String cancelReasonDesc, 
                            Boolean needRollbackStock, Boolean needRefund) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.productId = productId;
        this.flashSaleProductId = flashSaleProductId;
        this.quantity = quantity;
        this.cancelReason = cancelReason;
        this.cancelReasonDesc = cancelReasonDesc;
        this.needRollbackStock = needRollbackStock;
        this.needRefund = needRefund;
        this.cancelTime = System.currentTimeMillis();
    }
} 