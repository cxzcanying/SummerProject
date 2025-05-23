package com.flashsale.order.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单创建DTO
 * @author 21311
 */
@Data
public class OrderCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀商品ID
     */
    private Long flashSaleProductId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 订单号
     */
    private String orderNo;
} 