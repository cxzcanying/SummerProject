package com.flashsale.seckill.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 限流记录实体类
 * @author 21311
 */
@Data
public class RateLimitRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 限流键
     */
    private String keyName;

    /**
     * 访问次数
     */
    private Integer count;

    /**
     * 时间窗口开始时间戳
     */
    private Long windowStart;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;
} 