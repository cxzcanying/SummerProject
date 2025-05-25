package com.flashsale.product.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀商品视图对象
 * @author 21311
 */
@Data
public class FlashSaleProductVO implements Serializable {
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
     * 活动名称
     */
    private String activityName;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品副标题
     */
    private String productSubtitle;

    /**
     * 商品主图
     */
    private String productMainImage;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 秒杀价格
     */
    private BigDecimal flashSalePrice;

    /**
     * 折扣
     */
    private BigDecimal discount;

    /**
     * 秒杀库存
     */
    private Integer flashSaleStock;

    /**
     * 每人限购数量
     */
    private Integer flashSaleLimit;

    /**
     * 已用库存
     */
    private Integer stockUsed;

    /**
     * 剩余库存
     */
    private Integer remainingStock;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 活动开始时间
     */
    private Date startTime;

    /**
     * 活动结束时间
     */
    private Date endTime;

    /**
     * 活动状态：0-未开始，1-进行中，2-已结束，3-已取消
     */
    private Integer activityStatus;

    /**
     * 创建时间
     */
    private Date createTime;
} 