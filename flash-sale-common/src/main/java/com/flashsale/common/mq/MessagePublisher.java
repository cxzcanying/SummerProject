package com.flashsale.common.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 业务消息发布器
 * @author 21311
 */
@Slf4j
@Component
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class MessagePublisher {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // ==================== 秒杀相关消息发送 ====================
    
    /**
     * 发送秒杀请求消息 - 用于流量削峰
     *
     * @param message 消息内容
     */
    public void sendSeckillRequestMessage(Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SECKILL_TOPIC_EXCHANGE,
                    RabbitMQConfig.SECKILL_REQUEST_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        msg.getMessageProperties().setDeliveryMode(MessageProperties.DEFAULT_DELIVERY_MODE);
                        return msg;
                    }
            );
            log.info("发送秒杀请求消息成功，correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("发送秒杀请求消息失败", e);
            throw new RuntimeException("发送秒杀请求消息失败", e);
        }
    }

    /**
     * 发送秒杀成功消息
     *
     * @param message 消息内容
     */
    public void sendSeckillSuccessMessage(Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SECKILL_TOPIC_EXCHANGE,
                    RabbitMQConfig.SECKILL_SUCCESS_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.info("发送秒杀成功消息成功，correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("发送秒杀成功消息失败", e);
            throw new RuntimeException("发送秒杀成功消息失败", e);
        }
    }

    /**
     * 发送秒杀失败消息
     *
     * @param message 消息内容
     */
    public void sendSeckillFailedMessage(Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.SECKILL_TOPIC_EXCHANGE,
                    RabbitMQConfig.SECKILL_FAILED_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.info("发送秒杀失败消息成功，correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("发送秒杀失败消息失败", e);
            throw new RuntimeException("发送秒杀失败消息失败", e);
        }
    }

    // ==================== 库存相关消息发送 ====================
    
    /**
     * 发送库存扣减消息
     *
     * @param message 消息内容
     */
    public void sendStockDeductMessage(Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STOCK_EXCHANGE,
                    RabbitMQConfig.STOCK_DEDUCT_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.info("发送库存扣减消息成功，correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("发送库存扣减消息失败", e);
            throw new RuntimeException("发送库存扣减消息失败", e);
        }
    }

    /**
     * 发送库存回滚消息
     *
     * @param message 消息内容
     */
    public void sendStockRollbackMessage(Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.STOCK_EXCHANGE,
                    RabbitMQConfig.STOCK_ROLLBACK_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.info("发送库存回滚消息成功，correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("发送库存回滚消息失败", e);
            throw new RuntimeException("发送库存回滚消息失败", e);
        }
    }

    // ==================== 订单相关消息发送 ====================
    
    /**
     * 发送订单创建消息
     *
     * @param message 消息内容
     */
    public void sendOrderCreateMessage(Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATE_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.info("发送订单创建消息成功，correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("发送订单创建消息失败", e);
            throw new RuntimeException("发送订单创建消息失败", e);
        }
    }

    /**
     * 发送订单取消消息
     *
     * @param message 消息内容
     */
    public void sendOrderCancelMessage(Object message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_CANCEL_ROUTING_KEY,
                    message,
                    msg -> {
                        msg.getMessageProperties().setCorrelationId(correlationId);
                        return msg;
                    }
            );
            log.info("发送订单取消消息成功，correlationId: {}", correlationId);
        } catch (Exception e) {
            log.error("发送订单取消消息失败", e);
            throw new RuntimeException("发送订单取消消息失败", e);
        }
    }

    // ==================== 通用消息发送方法 ====================
    
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
} 