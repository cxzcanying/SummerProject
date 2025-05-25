package com.flashsale.product.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品数据传输对象
 * @author 21311
 */
@Data
public class ProductDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /**
     * 商品副标题
     */
    private String subtitle;

    /**
     * 主图片URL
     */
    private String mainImage;

    /**
     * 子图片URL，以逗号分隔
     */
    private String subImages;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 原价
     */
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格必须大于等于0")
    private BigDecimal price;

    /**
     * 库存
     */
    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存必须大于等于0")
    private Integer stock;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
} 