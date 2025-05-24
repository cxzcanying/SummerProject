package com.flashsale.common.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀请求消息DTO - 用于流量削峰
 * @author 21311
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillRequestMessage implements Serializable {
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
     * 商品ID
     */
    private Long productId;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 秒杀令牌
     */
    private String token;

    /**
     * 请求时间戳
     */
    private Long requestTime;

    /**
     * 请求来源：1-Web，2-App，3-小程序
     */
    private Integer source;

    /**
     * 用户IP
     */
    private String userIp;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 优先级：1-普通，2-VIP，3-超级VIP
     */
    private Integer priority;

    public SeckillRequestMessage(Long userId, Long flashSaleProductId, Long productId, Integer quantity, String token) {
        this.userId = userId;
        this.flashSaleProductId = flashSaleProductId;
        this.productId = productId;
        this.quantity = quantity;
        this.token = token;
        this.requestTime = System.currentTimeMillis();
        this.priority = 1;
        // 默认普通优先级
    }
} 