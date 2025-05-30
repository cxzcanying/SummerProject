package com.flashsale.order.service.impl;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.ResultCode;
import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.mq.MessageSender;
import com.flashsale.common.mq.message.PaymentProcessMessage;
import com.flashsale.common.mq.RabbitMQConfig;
import com.flashsale.order.entity.FlashSaleOrder;
import com.flashsale.order.mapper.FlashSaleOrderMapper;
import com.flashsale.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * 订单服务实现类
 * @author 21311
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private FlashSaleOrderMapper flashSaleOrderMapper;
    
    @Autowired
    private MessageSender messageSender;

    @Autowired
    @Qualifier("orderRabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<FlashSaleOrder> createSeckillOrder(SeckillDTO seckillDTO) {
        try {
            FlashSaleOrder order = new FlashSaleOrder();
            
            // 设置订单信息
            order.setOrderNo(generateOrderNo());
            order.setUserId(seckillDTO.getUserId());
            order.setFlashSaleProductId(seckillDTO.getFlashSaleProductId());
            order.setQuantity(seckillDTO.getQuantity());
            
            order.setProductName("秒杀商品");
            order.setProductImage("default.jpg");
            order.setFlashSalePrice(new BigDecimal("9.99"));
            
            // 计算支付金额
            order.setPaymentAmount(order.getFlashSalePrice().multiply(new BigDecimal(order.getQuantity())));
            
            // 设置订单状态
            order.setStatus(0);
            
            // 设置订单状态
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            
            // 保存订单
            int rows = flashSaleOrderMapper.insert(order);
            if (rows != 1) {
                return Result.error(ResultCode.ERROR.getCode(), "创建订单失败");
            }
            
            log.info("成功创建秒杀订单，订单号：{}", order.getOrderNo());
            return Result.success(order);
            
        } catch (Exception e) {
            log.error("创建秒杀订单失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "创建订单失败");
        }
    }

    @Override
    public Result<FlashSaleOrder> getOrderByOrderNo(String orderNo) {
        try {
            FlashSaleOrder order = flashSaleOrderMapper.findByOrderNo(orderNo);
            if (order == null) {
                return Result.error(ResultCode.ORDER_NOT_EXIST.getCode(), "订单不存在");
            }
            return Result.success(order);
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "查询订单失败");
        }
    }

    @Override
    public Result<List<FlashSaleOrder>> getOrdersByUserId(Long userId) {
        try {
            List<FlashSaleOrder> orders = flashSaleOrderMapper.findByUserId(userId);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("查询用户订单列表失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "查询订单列表失败");
        }
    }

    @Override
    public Result<List<FlashSaleOrder>> getUserPendingOrders(Long userId) {
        try {
            List<FlashSaleOrder> orders = flashSaleOrderMapper.findPendingOrdersByUserId(userId);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("查询用户待付款订单列表失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "查询待付款订单列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> payOrder(String orderNo, Integer payType) {
        try {
            FlashSaleOrder order = flashSaleOrderMapper.findByOrderNo(orderNo);
            if (order == null) {
                return Result.error(ResultCode.ORDER_NOT_EXIST.getCode(), "订单不存在");
            }
            
            if (order.getStatus() != 0) {
                return Result.error(ResultCode.ORDER_STATUS_ERROR.getCode(), "订单状态错误");
            }
            
            // 检查订单是否超过30分钟
            if (order.getCreateTime() != null) {
                Date expireTime = new Date(order.getCreateTime().getTime() + 30 * 60 * 1000);
                if (new Date().after(expireTime)) {
                    // 自动取消过期订单
                    flashSaleOrderMapper.updateStatus(order.getId(), 4); // 已取消
                    return Result.error(ResultCode.ORDER_EXPIRED.getCode(), "订单已过期");
                }
            }
            
            // 更新订单状态为已支付
            order.setStatus(1);
            order.setPaymentType(payType);
            order.setPaymentTime(new Date());
            order.setUpdateTime(new Date());
            
            // 生成支付平台交易ID
            order.setTransactionId("PAY" + System.currentTimeMillis());
            
            int rows = flashSaleOrderMapper.updateById(order);
            if (rows != 1) {
                return Result.error(ResultCode.ERROR.getCode(), "支付失败");
            }
            
            // 发送支付处理消息到RabbitMQ
            try {
                // 创建支付消息对象 - 使用HashMap替代PaymentProcessMessage类
                Map<String, Object> paymentMessage = new HashMap<>();
                paymentMessage.put("orderNo", orderNo);
                paymentMessage.put("userId", order.getUserId());
                paymentMessage.put("amount", order.getPaymentAmount());
                paymentMessage.put("paymentMethod", payType);
                
                // 直接发送消息到RabbitMQ
                rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE, 
                    RabbitMQConfig.PAYMENT_PROCESS_ROUTING_KEY, 
                    paymentMessage
                );
                
                log.info("支付处理消息发送成功，订单号：{}", orderNo);
            } catch (Exception e) {
                log.error("支付处理消息发送失败，但订单状态已更新，订单号：{}", orderNo, e);
                // 消息发送失败，但不影响订单状态更新
            }
            
            log.info("订单支付成功，订单号：{}", orderNo);
            return Result.success();
            
        } catch (Exception e) {
            log.error("订单支付失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "支付失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrder(String orderNo) {
        try {
            FlashSaleOrder order = flashSaleOrderMapper.findByOrderNo(orderNo);
            if (order == null) {
                return Result.error(ResultCode.ORDER_NOT_EXIST.getCode(), "订单不存在");
            }
            
            if (order.getStatus() != 0) {
                return Result.error(ResultCode.ORDER_STATUS_ERROR.getCode(), "订单状态错误，无法取消");
            }
            
            // 更新订单状态为已取消
            int rows = flashSaleOrderMapper.updateStatus(order.getId(), 4);
            if (rows != 1) {
                return Result.error(ResultCode.ERROR.getCode(), "取消订单失败");
            }
            
            log.info("订单取消成功，订单号：{}", orderNo);
            return Result.success();
            
        } catch (Exception e) {
            log.error("取消订单失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "取消订单失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> completeOrder(String orderNo) {
        try {
            FlashSaleOrder order = flashSaleOrderMapper.findByOrderNo(orderNo);
            if (order == null) {
                return Result.error(ResultCode.ORDER_NOT_EXIST.getCode(), "订单不存在");
            }
            
            if (order.getStatus() != 1 && order.getStatus() != 2) {
                return Result.error(ResultCode.ORDER_STATUS_ERROR.getCode(), "订单状态错误，只有已支付或已发货订单才能完成");
            }
            
            // 更新订单状态为已完成
            int rows = flashSaleOrderMapper.updateStatus(order.getId(), 3);
            if (rows != 1) {
                return Result.error(ResultCode.ERROR.getCode(), "完成订单失败");
            }
            
            log.info("订单完成成功，订单号：{}", orderNo);
            return Result.success();
            
        } catch (Exception e) {
            log.error("完成订单失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "完成订单失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> handleExpiredOrder(String orderNo) {
        try {
            FlashSaleOrder order = flashSaleOrderMapper.findByOrderNo(orderNo);
            if (order == null) {
                return Result.error(ResultCode.ORDER_NOT_EXIST.getCode(), "订单不存在");
            }
            
            if (order.getStatus() == 0 && order.getCreateTime() != null) {
                // 计算过期时间（创建时间30分钟后）
                Date expireTime = new Date(order.getCreateTime().getTime() + 30 * 60 * 1000);
                
                if (new Date().after(expireTime)) {
                    // 自动取消过期订单
                    flashSaleOrderMapper.updateStatus(order.getId(), 5); // 已超时
                    log.info("自动取消过期订单，订单号：{}", orderNo);
                }
            }
            
            return Result.success();
            
        } catch (Exception e) {
            log.error("处理过期订单失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "处理过期订单失败");
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "FS" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
} 