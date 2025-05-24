package com.flashsale.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 库存扣减消息DTO
 * @author 21311
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductMessage implements Serializable {
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
     * 扣减数量
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
     * 业务类型：1-秒杀扣减，2-订单扣减，3-预扣减
     */
    private Integer businessType;

    /**
     * 扣减类型：1-实际库存，2-预扣库存
     */
    private Integer deductType;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 创建时间戳
     */
    private Long timestamp;

    /**
     * 备注信息
     */
    private String remark;

    public StockDeductMessage(Long productId, Long flashSaleProductId, Integer quantity, String orderNo, Long userId) {
        this.productId = productId;
        this.flashSaleProductId = flashSaleProductId;
        this.quantity = quantity;
        this.orderNo = orderNo;
        this.userId = userId;
        this.businessType = 1;
        // 默认秒杀扣减
        this.deductType = 1;
        // 默认实际库存
        this.timestamp = System.currentTimeMillis();
    }
} 