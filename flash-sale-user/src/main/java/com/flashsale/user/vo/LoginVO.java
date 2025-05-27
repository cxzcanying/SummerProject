package com.flashsale.user.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录响应视图对象
 * @author 21311
 */
@Data
public class LoginVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 认证令牌
     */
    private String token;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;
} 