package com.flashsale.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 库存回滚消息DTO
 * @author 21311
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRollbackMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀商品ID
     */
    private Long flashSaleProductId;

    /**
     * 回滚数量
     */
    private Integer quantity;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 回滚原因
     */
    private String reason;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 创建时间戳
     */
    private Long timestamp;

    public StockRollbackMessage(Long productId, Long flashSaleProductId, Integer quantity, String orderNo, String reason) {
        this.productId = productId;
        this.flashSaleProductId = flashSaleProductId;
        this.quantity = quantity;
        this.orderNo = orderNo;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }
} 