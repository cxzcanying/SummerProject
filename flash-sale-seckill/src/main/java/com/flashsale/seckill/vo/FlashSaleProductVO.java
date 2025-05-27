package com.flashsale.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀商品视图对象
 * @author 21311
 */
@Data
public class FlashSaleProductVO implements Serializable {
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
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

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
    private Integer flashSaleLimit;

    /**
     * 已售数量
     */
    private Integer stockUsed;

    /**
     * 剩余库存
     */
    private Integer remainingStock;

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
     * 状态名称
     */
    private String statusName;

    /**
     * 是否可以秒杀
     */
    private Boolean canSeckill;

    /**
     * 秒杀进度（百分比）
     */
    private Integer progress;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
} 