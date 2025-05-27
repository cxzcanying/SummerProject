package com.flashsale.seckill.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀订单实体类
 * @author 21311
 */
@Data
public class SeckillOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

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
     * 商品名称
     */
    private String productName;

    /**
     * 商品主图
     */
    private String productImage;

    /**
     * 秒杀价格
     */
    private BigDecimal flashSalePrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;

    /**
     * 支付时间
     */
    private Date paymentTime;

    /**
     * 支付方式：1-支付宝，2-微信
     */
    private Integer paymentType;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消，5-已超时
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

    // 兼容字段，用于与老代码兼容，但不会保存到数据库
    // 在DTO转换等处理中保留这些字段以避免NPE
    
    /**
     * 原价（兼容字段）
     */
    private transient BigDecimal originalPrice;
    
    /**
     * 实付金额（兼容字段）
     */
    private transient BigDecimal payAmount;
    
    /**
     * 支付方式（兼容字段）
     */
    private transient Integer payType;
    
    /**
     * 支付时间（兼容字段）
     */
    private transient Date payTime;
    
    /**
     * 优惠金额（兼容字段）
     */
    private transient BigDecimal discountAmount;
    
    /**
     * 过期时间（兼容字段）
     */
    private transient Date expireTime;
} 