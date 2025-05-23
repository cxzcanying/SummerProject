package com.flashsale.common.result;

import lombok.Getter;

/**
 * 标准API状态码
 * @author 21311
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "Success"),
    ERROR(500, "System error"),

    UNAUTHORIZED(401, "Not authenticated"),
    FORBIDDEN(403, "Access denied"),
    
    // Business errors
    PARAM_ERROR(1001, "Parameter error"),
    USER_NOT_EXIST(1002, "User does not exist"),
    USER_ALREADY_EXIST(1003, "User already exists"),
    PASSWORD_ERROR(1004, "Password error"),
    
    // Product errors
    PRODUCT_NOT_EXIST(2001, "Product does not exist"),
    PRODUCT_STOCK_NOT_ENOUGH(2002, "Product stock not enough"),
    
    // Flash sale errors
    SECKILL_NOT_START(3001, "Flash sale has not started"),
    SECKILL_ENDED(3002, "Flash sale has ended"),
    SECKILL_REPEATED(3003, "You have already participated in this flash sale"),
    SECKILL_FAILED(3004, "Flash sale failed"),
    SECKILL_RATE_LIMIT(3005, "System is busy, please try again later"),
    
    // Order errors
    ORDER_CREATE_FAILED(4001, "Order creation failed"),
    ORDER_NOT_EXIST(4002, "Order does not exist"),
    ORDER_ALREADY_PAID(4003, "Order has already been paid"),
    ORDER_STATUS_ERROR(4004, "Order status error"),
    ORDER_EXPIRED(4005, "Order has expired"),
    
    // Payment errors
    PAYMENT_FAILED(5001, "Payment failed"),
    
    // Coupon errors
    COUPON_NOT_EXIST(6001, "Coupon does not exist"),
    COUPON_EXPIRED(6002, "Coupon has expired"),
    COUPON_ALREADY_USED(6003, "Coupon has already been used"),
    COUPON_NOT_ENOUGH(6004, "Coupon stock not enough");
    
    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
} 