package com.flashsale.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀请求参数
 * @author 21311
 */
@Data
public class SeckillDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 秒杀商品ID
     */
    private Long flashSaleProductId;

    /**
     * 购买数量
     */
    private Integer quantity = 1;

    /**
     * 秒杀令牌
     */
    private String token;
} 