package com.flashsale.common.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;

/**
 * 秒杀数据传输对象
 * @author 21311
 */
@Data
public class SeckillDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 秒杀商品ID
     */
    @NotNull(message = "秒杀商品ID不能为空")
    private Long flashSaleProductId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    private Integer quantity;

    /**
     * 优惠券ID（可选）
     */
    private Long couponId;

    /**
     * 用户令牌
     */
    @NotNull(message = "用户令牌不能为空")
    private String token;
} 