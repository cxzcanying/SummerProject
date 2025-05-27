package com.flashsale.seckill.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 秒杀商品DTO
 * @author 21311
 */
@Data
public class FlashSaleProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 秒杀价格
     */
    @NotNull(message = "秒杀价格不能为空")
    @DecimalMin(value = "0.01", message = "秒杀价格必须大于0")
    private BigDecimal flashSalePrice;

    /**
     * 秒杀库存
     */
    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 1, message = "秒杀库存必须大于0")
    private Integer flashSaleStock;

    /**
     * 每人限购数量
     */
    @NotNull(message = "每人限购数量不能为空")
    @Min(value = 1, message = "每人限购数量必须大于0")
    private Integer flashSaleLimit;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private Date startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private Date endTime;
} 