package com.flashsale.common.mq;

import com.flashsale.common.mq.message.PaymentProcessMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 消息发送器
 * @author 21311
 */
@Slf4j
@Component
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class MessageSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息到指定交换器和路由键
     *
     * @param exchange 交换器名称
     * @param routingKey 路由键
     * @param message 消息内容
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setCorrelationId(correlationId);
                return msg;
            });
            log.info("发送消息成功，exchange: {}, routingKey: {}, correlationId: {}", 
                    exchange, routingKey, correlationId);
        } catch (Exception e) {
            log.error("发送消息失败，exchange: {}, routingKey: {}", exchange, routingKey, e);
            throw new RuntimeException("发送消息失败", e);
        }
    }

    /**
     * 发送带重试机制的消息
     *
     * @param exchange 交换器名称
     * @param routingKey 路由键
     * @param message 消息内容
     * @param retryCount 重试次数
     */
    public void sendMessageWithRetry(String exchange, String routingKey, Object message, int retryCount) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
                msg.getMessageProperties().setCorrelationId(correlationId);
                msg.getMessageProperties().setHeader("retryCount", retryCount);
                msg.getMessageProperties().setHeader("maxRetryCount", RabbitMQConfig.MAX_RETRY_COUNT);
                return msg;
            });
            log.info("发送带重试机制的消息成功，exchange: {}, routingKey: {}, correlationId: {}, retryCount: {}", 
                    exchange, routingKey, correlationId, retryCount);
        } catch (Exception e) {
            log.error("发送带重试机制的消息失败，exchange: {}, routingKey: {}, retryCount: {}", 
                    exchange, routingKey, retryCount, e);
            throw new RuntimeException("发送带重试机制的消息失败", e);
        }
    }

    /**
     * 发送死信消息
     *
     * @param originalMessage 原始消息
     * @param errorReason 错误原因
     */
    public void sendToDeadLetterQueue(Object originalMessage, String errorReason) {
        try {
            String correlationId = UUID.randomUUID().toString();
            
            // 创建死信消息包装对象
            DeadLetterMessageWrapper deadLetterMessage = new DeadLetterMessageWrapper();
            deadLetterMessage.setOriginalMessage(originalMessage);
            deadLetterMessage.setErrorReason(errorReason);
            deadLetterMessage.setFailureTime(System.currentTimeMillis());
            deadLetterMessage.setCorrelationId(correlationId);
            
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.DLX_EXCHANGE,
                    RabbitMQConfig.DEAD_LETTER_ROUTING_KEY,
                    deadLetterMessage,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.warn("发送死信消息，correlationId: {}, errorReason: {}", correlationId, errorReason);
        } catch (Exception e) {
            log.error("发送死信消息失败，errorReason: {}", errorReason, e);
        }
    }

    /**
     * 发送支付处理消息
     *
     * @param paymentProcessMessage 支付处理消息
     */
    public void sendPaymentProcessMessage(PaymentProcessMessage paymentProcessMessage) {
        try {
            log.info("发送支付处理消息，订单号：{}", paymentProcessMessage.getOrderNo());
            sendMessage(RabbitMQConfig.PAYMENT_EXCHANGE, RabbitMQConfig.PAYMENT_PROCESS_ROUTING_KEY, paymentProcessMessage);
        } catch (Exception e) {
            log.error("发送支付处理消息失败，订单号：{}", paymentProcessMessage.getOrderNo(), e);
            throw new RuntimeException("发送支付处理消息失败", e);
        }
    }

    /**
     * 死信消息包装类
     */
    public static class DeadLetterMessageWrapper {
        private Object originalMessage;
        private String errorReason;
        private long failureTime;
        private String correlationId;

        // Getters and Setters
        public Object getOriginalMessage() {
            return originalMessage;
        }

        public void setOriginalMessage(Object originalMessage) {
            this.originalMessage = originalMessage;
        }

        public String getErrorReason() {
            return errorReason;
        }

        public void setErrorReason(String errorReason) {
            this.errorReason = errorReason;
        }

        public long getFailureTime() {
            return failureTime;
        }

        public void setFailureTime(long failureTime) {
            this.failureTime = failureTime;
        }

        public String getCorrelationId() {
            return correlationId;
        }

        public void setCorrelationId(String correlationId) {
            this.correlationId = correlationId;
        }
    }
} 