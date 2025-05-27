package com.flashsale.seckill.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀活动DTO
 * @author 21311
 */
@Data
public class FlashSaleActivityDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空")
    private String name;

    /**
     * 活动描述
     */
    private String description;

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