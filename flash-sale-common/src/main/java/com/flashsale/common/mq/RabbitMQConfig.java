package com.flashsale.common.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * @author 21311
 */
@Slf4j
@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    // ==================== 交换机名称常量 ====================
    
    /**
     * 支付操作交换机 - 用于支付相关操作
     */
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    // ==================== 队列名称常量 ====================
    
    /**
     * 订单状态更新队列
     */
    public static final String ORDER_STATUS_UPDATE_QUEUE = "order.status.update.queue";

    /**
     * 支付处理队列
     */
    public static final String PAYMENT_PROCESS_QUEUE = "payment.process.queue";

    // ==================== 路由键常量 ====================
    
    /**
     * 订单状态更新路由键
     */
    public static final String ORDER_STATUS_UPDATE_ROUTING_KEY = "order.status.update";

    /**
     * 支付处理路由键
     */
    public static final String PAYMENT_PROCESS_ROUTING_KEY = "payment.process";

    // ==================== 其他常量 ====================
    
    /**
     * 最大重试次数
     */
    public static final int MAX_RETRY_COUNT = 3;

    // ==================== RabbitTemplate和Factory配置 ====================
    
    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        // 设置消息序列化器
        
        // 开启发送方确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息发送失败，correlationData: {}, cause: {}", correlationData, cause);
            } else {
                log.debug("消息发送成功，correlationData: {}", correlationData);
            }
        });
        // 发送确认机制
        
        // 开启发送方回调
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息投递失败，returned message: {}, replyCode: {}, replyText: {}, exchange: {}, routingKey: {}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });
        // 投递失败回调，用于处理消息无法路由到队列的情况
        
        // 设置mandatory为true，确保消息必须能路由到队列，当消息无法路由到队列时会触发return callback
        rabbitTemplate.setMandatory(true);
        
        return rabbitTemplate;
    }

    /**
     * 配置RabbitListenerContainerFactory
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        // 设置消息序列化器
        
        // 配置预取数量（每个消费者预取的消息数量）
        factory.setPrefetchCount(10);
        // 可自定义
        
        // 配置并发消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        //最多10个并发消费者，根据负载自动扩缩容
        
        // 配置手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        // 配置拒绝消息后不重新入队
        factory.setDefaultRequeueRejected(false);
        
        return factory;
    }

    // ==================== 交换机配置 ====================
    
    /**
     * 支付操作直连交换机
     */
    @Bean
    public DirectExchange paymentExchange() {
        return ExchangeBuilder.directExchange(PAYMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 队列配置 ====================
    
    /**
     * 订单状态更新队列
     */
    @Bean
    public Queue orderStatusUpdateQueue() {
        return QueueBuilder.durable(ORDER_STATUS_UPDATE_QUEUE)
                .build();
    }

    /**
     * 支付处理队列
     */
    @Bean
    public Queue paymentProcessQueue() {
        return QueueBuilder.durable(PAYMENT_PROCESS_QUEUE)
                .build();
    }

    // ==================== 绑定配置 ====================
    
    /**
     * 绑定订单状态更新队列
     */
    @Bean
    public Binding orderStatusUpdateBinding() {
        return BindingBuilder.bind(orderStatusUpdateQueue())
                .to(paymentExchange())
                .with(ORDER_STATUS_UPDATE_ROUTING_KEY);
    }

    /**
     * 绑定支付处理队列
     */
    @Bean
    public Binding paymentProcessBinding() {
        return BindingBuilder.bind(paymentProcessQueue())
                .to(paymentExchange())
                .with(PAYMENT_PROCESS_ROUTING_KEY);
    }
} 