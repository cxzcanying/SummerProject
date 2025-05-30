package com.flashsale.seckill.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.common.mq.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付消息生产者
 * @author 21311
 */
@Slf4j
@Component
public class PaymentMessageProducer {

    @Autowired
    private AmqpTemplate amqpTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送异步支付消息
     */
    public void sendPaymentMessage(String orderNo, Long userId, BigDecimal amount, Integer paymentMethod) {
        try {
            Map<String, Object> paymentMessage = new HashMap<>();
            paymentMessage.put("orderNo", orderNo);
            paymentMessage.put("userId", userId);
            paymentMessage.put("amount", amount);
            paymentMessage.put("paymentMethod", paymentMethod);
            paymentMessage.put("timestamp", System.currentTimeMillis());
            
            String messageJson = objectMapper.writeValueAsString(paymentMessage);
            
            log.info("发送支付消息: {}", messageJson);
            
            amqpTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_PROCESS_ROUTING_KEY,
                messageJson
            );
            
            log.info("支付消息发送成功，订单号: {}", orderNo);
        } catch (Exception e) {
            log.error("发送支付消息失败，订单号: {}", orderNo, e);
            throw new RuntimeException("发送支付消息失败", e);
        }
    }
} 