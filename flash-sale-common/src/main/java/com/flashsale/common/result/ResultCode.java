package com.flashsale.common.result;

import lombok.Getter;

/**
 * 标准API状态码
 * @author 21311
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
public enum ResultCode {
    SUCCESS(200, "Success"),
    ERROR(500, "System error"),
    UNAUTHORIZED(401, "Not authenticated"),
    FORBIDDEN(403, "Access denied"),
    
    // 其他错误
    PARAM_ERROR(1001, "Parameter error"),
    USER_NOT_EXIST(1002, "User does not exist"),
    USER_ALREADY_EXIST(1003, "User already exists"),
    PASSWORD_ERROR(1004, "Password error"),
    
    // 商品错误
    PRODUCT_NOT_EXIST(2001, "Product does not exist"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "Product stock not enough"),
    
    // 秒杀错误
    SECKILL_NOT_START(3001, "Flash sale has not started"),
    SECKILL_ENDED(3002, "Flash sale has ended"),
    SECKILL_REPEATED(3003, "You have already participated in this flash sale"),
    SECKILL_FAILED(3004, "Flash sale failed"),
    SECKILL_RATE_LIMIT(3005, "System is busy, please try again later"),
    
    // 订单错误
    ORDER_CREATE_FAILED(4001, "Order creation failed"),
    ORDER_NOT_EXIST(4002, "Order does not exist"),
    ORDER_ALREADY_PAID(4003, "Order has already been paid"),
    ORDER_STATUS_ERROR(4004, "Order status error"),
    ORDER_EXPIRED(4005, "Order has expired"),

    /**
     * 支付错误
     */
    PAYMENT_FAILED(5001, "Payment failed");

    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
} 