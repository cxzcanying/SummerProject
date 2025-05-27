package com.flashsale.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀订单视图对象
 * @author 21311
 */
@Data
public class SeckillOrderVO implements Serializable {
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
     * 活动名称
     */
    private String activityName;

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
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 秒杀价格
     */
    private BigDecimal flashSalePrice;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已完成
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 支付方式：1-支付宝，2-微信，3-银行卡
     */
    private Integer payType;

    /**
     * 支付方式描述
     */
    private String payTypeDesc;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 剩余支付时间（秒）
     */
    private Long remainPayTime;
} 