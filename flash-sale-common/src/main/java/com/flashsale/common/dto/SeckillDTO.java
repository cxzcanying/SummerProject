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
    //编译时检查注解，帮助编译器验证序列化相关的代码是否正确

    private static final long serialVersionUID = 1L;
    //序列化版本号，防止因字段修改导致反序列化失败，若序列化版本UID一样会尝试进行兼容

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

/*
  其中商品ID和活动ID会冗余，违反了DRY原则，但这样设计避免重复查询数据库的操作，可以减轻数据库压力
*/

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
     * 用户令牌
     */
    @NotNull(message = "用户令牌不能为空")
    private String token;
} 