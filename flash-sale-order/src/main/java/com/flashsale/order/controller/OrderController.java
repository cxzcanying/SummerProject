package com.flashsale.order.controller;

import com.flashsale.common.result.Result;
import com.flashsale.order.entity.FlashSaleOrder;
import com.flashsale.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 * @author 21311
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 根据订单号查询订单详情
     */
    @GetMapping("/detail/{orderNo}")
    public Result<FlashSaleOrder> getOrder(@PathVariable String orderNo) {
        return orderService.getOrderByOrderNo(orderNo);
    }

    /**
     * 根据用户ID查询订单列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<FlashSaleOrder>> getUserOrders(@PathVariable Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    /**
     * 查询用户待付款订单
     */
    @GetMapping("/user/{userId}/pending")
    public Result<List<FlashSaleOrder>> getUserPendingOrders(@PathVariable Long userId) {
        return orderService.getUserPendingOrders(userId);
    }


    /**
     * 支付订单
     */
    @PostMapping("/{orderNo}/pay")
    public Result<Void> payOrder(@PathVariable String orderNo, @RequestParam Integer payType) {
        return orderService.payOrder(orderNo, payType);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        return orderService.cancelOrder(orderNo);
    }

    // 内部类定义
    public static class ApplyCouponRequest {
        private String orderId;
        private Long couponId;
        
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public Long getCouponId() { return couponId; }
        public void setCouponId(Long couponId) { this.couponId = couponId; }
    }
} 