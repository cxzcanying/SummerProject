package com.flashsale.product.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀商品实体类
 * @author 21311
 */
@Data
public class FlashSaleProduct implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 秒杀商品ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 秒杀价格
     */
    private BigDecimal flashSalePrice;

    /**
     * 秒杀库存
     */
    private Integer flashSaleStock;

    /**
     * 每人限购数量
     */
    private Integer limitPerUser;

    /**
     * 已售数量
     */
    private Integer soldCount;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
} 