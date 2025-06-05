package com.flashsale.payment.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.common.mq.RabbitMQConfig;
import com.flashsale.common.result.Result;
import com.flashsale.payment.dto.PaymentDTO;
import com.flashsale.payment.service.PaymentService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * 支付处理消息监听器 - 直接处理消息内容
 * @author 21311
 */
@Slf4j
@Component
public class PaymentProcessListener {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_PROCESS_QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("收到支付处理消息，开始解析");
            
            // 获取消息内容
            String messageContent = new String(message.getBody());
            log.info("消息内容: {}", messageContent);
            
            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(messageContent);
            
            // 提取必要字段
            String orderNo = jsonNode.has("orderNo") ? jsonNode.get("orderNo").asText() : null;
            Long userId = jsonNode.has("userId") ? jsonNode.get("userId").asLong() : null;
            BigDecimal amount = jsonNode.has("amount") ? 
                    new BigDecimal(jsonNode.get("amount").asText()) : null;
            Integer paymentMethod = jsonNode.has("paymentMethod") ? 
                    jsonNode.get("paymentMethod").asInt() : 2;
            // 默认微信支付
            
            // 验证必要字段
            if (orderNo == null || userId == null || amount == null) {
                log.error("消息缺少必要字段: orderNo={}, userId={}, amount={}", orderNo, userId, amount);
                // 拒绝消息并不重新入队
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            
            log.info("解析成功: 订单号={}, 用户ID={}, 金额={}, 支付方式={}", 
                    orderNo, userId, amount, paymentMethod);
            
            // 创建支付DTO
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setOrderId(generateOrderId(orderNo));
            paymentDTO.setUserId(userId);
            paymentDTO.setAmount(amount);
            paymentDTO.setPaymentMethod(paymentMethod);
            
            // 调用支付服务创建支付记录
            Result<?> result = paymentService.createPayment(paymentDTO);
            
            if (result.getCode() == 200) {
                log.info("支付处理成功，订单号: {}，消息: {}", orderNo, result.getMessage());
                // 确认消息
                channel.basicAck(deliveryTag, false);
            } else {
                log.error("支付处理失败，订单号: {}，错误信息: {}", orderNo, result.getMessage());
                // 拒绝消息并不重新入队（避免无限循环）
                channel.basicNack(deliveryTag, false, false);
            }
            
        } catch (Exception e) {
            log.error("支付处理失败: {}", e.getMessage(), e);
            
            // 拒绝消息并不重新入队（避免无限循环）
            channel.basicNack(deliveryTag, false, false);
        }
    }
    
    /**
     * 从订单号生成订单ID
     * 使用订单号的哈希值结合当前时间戳，确保唯一性
     */
    private Long generateOrderId(String orderNo) {
        try {
            if (orderNo != null && orderNo.startsWith("FS")) {
                // 从订单号中提取时间戳部分
                String timestampStr = orderNo.substring(2, Math.min(15, orderNo.length()));
                Long timestamp = Long.parseLong(timestampStr);
                
                // 结合订单号的哈希值，确保唯一性
                int hashCode = Math.abs(orderNo.hashCode());
                // 取哈希值的后6位，避免太大
                long hashSuffix = hashCode % 1000000;
                
                // 组合时间戳和哈希值
                return timestamp * 1000000 + hashSuffix;
            }
            
            // 如果订单号格式不正确，使用当前时间戳
            return System.currentTimeMillis();
        } catch (Exception e) {
            log.warn("无法从订单号解析订单ID，使用时间戳代替: {}，错误: {}", orderNo, e.getMessage());
            return System.currentTimeMillis();
        }
    }
} 