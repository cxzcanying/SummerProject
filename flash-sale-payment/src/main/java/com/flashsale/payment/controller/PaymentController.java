package com.flashsale.payment.controller;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.payment.dto.PaymentDTO;
import com.flashsale.payment.service.PaymentService;
import com.flashsale.payment.vo.PaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 支付控制器
 * @author 21311
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付订单
     */
    @PostMapping("/create")
    public Result<PaymentVO> createPayment(@Valid @RequestBody PaymentDTO paymentDTO) {
        return paymentService.createPayment(paymentDTO);
    }

    /**
     * 支付回调接口
     */
    @PostMapping("/callback")
    public Result<String> paymentCallback(
            @RequestParam String paymentNo,
            @RequestParam String thirdPartyPaymentNo,
            @RequestParam Integer status) {
        return paymentService.handlePaymentCallback(paymentNo, thirdPartyPaymentNo, status);
    }

    /**
     * 查询支付详情
     */
    @GetMapping("/detail/{paymentNo}")
    public Result<PaymentVO> getPaymentDetail(@PathVariable String paymentNo) {
        log.info("查询支付详情，支付流水号：{}", paymentNo);
        return paymentService.getPaymentDetail(paymentNo);
    }

    /**
     * 调试接口 - 查询数据库中的所有支付记录
     */
    @GetMapping("/debug/all")
    public Result<List<PaymentVO>> getAllPayments() {
        log.info("调试：查询所有支付记录");
        return paymentService.getAllPaymentsForDebug();
    }

    /**
     * 根据订单ID查询支付记录
     */
    @GetMapping("/order/{orderId}")
    public Result<PaymentVO> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("查询订单，订单号：{}", orderId);
        return paymentService.getPaymentByOrderId(orderId);
    }

    /**
     * 分页查询用户支付记录
     */
    @GetMapping("/user/{userId}")
    public Result<PageResult<PaymentVO>> getUserPayments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return paymentService.getUserPayments(userId, page, size);
    }

    /**
     * 申请退款
     */
    @PostMapping("/refund")
    public Result<String> applyRefund(
            @RequestParam String paymentNo,
            @RequestParam BigDecimal refundAmount,
            @RequestParam String refundReason) {
        return paymentService.applyRefund(paymentNo, refundAmount, refundReason);
    }

    /**
     * 退款回调接口
     */
    @PostMapping("/refund/callback")
    public Result<String> refundCallback(
            @RequestParam String refundNo,
            @RequestParam Integer status) {
        return paymentService.handleRefundCallback(refundNo, status);
    }

    /**
     * 取消支付
     */
    @PostMapping("/cancel/{paymentNo}")
    public Result<String> cancelPayment(@PathVariable String paymentNo) {
        return paymentService.cancelPayment(paymentNo);
    }
    //TODO 完成支付接口调用
} 