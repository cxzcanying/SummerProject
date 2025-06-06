package com.flashsale.payment.feign;

import com.flashsale.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 订单服务Feign客户端
 * @author 21311
 */
@FeignClient(name = "flash-sale-order", path = "/api/order")
public interface OrderServiceClient {

    /**
     * 根据订单ID获取订单号
     *
     * @param orderId 订单ID
     * @return 订单号
     */
    @GetMapping("/internal/orderNo/{orderId}")
    Result<String> getOrderNoByOrderId(@PathVariable("orderId") Long orderId);
} 