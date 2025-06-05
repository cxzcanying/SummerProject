package com.flashsale.common.exception;

import com.flashsale.common.result.ResultCode;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 交易异常
 * @author 21311
 */
@Getter
public class BusinessException extends RuntimeException implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String message;

    public BusinessException(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }
} 