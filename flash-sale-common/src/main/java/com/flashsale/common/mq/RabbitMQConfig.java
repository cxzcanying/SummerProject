package com.flashsale.common.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ基础配置类 - 提供常量定义和基础配置方法
 * 各服务可以继承此类来获取通用的配置方法和常量
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

    /**
     * 死信交换机 - 用于处理失败的消息
     */
    public static final String DEAD_LETTER_EXCHANGE = "dlx.exchange";

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

    // ==================== 工厂方法（供子类使用）====================
    
    /**
     * 创建支付操作直连交换机
     */
    protected DirectExchange createPaymentExchange() {
        return ExchangeBuilder.directExchange(PAYMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 创建死信交换机
     */
    protected DirectExchange createDeadLetterExchange() {
        return ExchangeBuilder.directExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 创建订单状态更新队列
     */
    protected Queue createOrderStatusUpdateQueue() {
        return QueueBuilder.durable(ORDER_STATUS_UPDATE_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .build();
    }

    /**
     * 创建支付处理队列
     */
    protected Queue createPaymentProcessQueue() {
        return QueueBuilder.durable(PAYMENT_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", "dlx.exchange")
                .build();
    }

    /**
     * 创建订单状态更新绑定
     */
    protected Binding createOrderStatusUpdateBinding(DirectExchange paymentExchange, Queue orderStatusUpdateQueue) {
        return BindingBuilder.bind(orderStatusUpdateQueue)
                .to(paymentExchange)
                .with(ORDER_STATUS_UPDATE_ROUTING_KEY);
    }

    /**
     * 创建支付处理绑定
     */
    protected Binding createPaymentProcessBinding(DirectExchange paymentExchange, Queue paymentProcessQueue) {
        return BindingBuilder.bind(paymentProcessQueue)
                .to(paymentExchange)
                .with(PAYMENT_PROCESS_ROUTING_KEY);
    }

    /**
     * 配置基础RabbitTemplate（包含发送确认和回调机制）
     * 各服务可以调用此方法来创建具有统一配置的RabbitTemplate
     */
    protected RabbitTemplate createBaseRabbitTemplate(ConnectionFactory connectionFactory, 
                                                     Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        
        // 开启发送方确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息发送失败，correlationData: {}, cause: {}", correlationData, cause);
            } else {
                log.debug("消息发送成功，correlationData: {}", correlationData);
            }
        });
        
        // 开启发送方回调
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息投递失败，returned message: {}, replyCode: {}, replyText: {}, exchange: {}, routingKey: {}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
        });
        
        // 设置mandatory为true，确保消息必须能路由到队列
        rabbitTemplate.setMandatory(true);
        
        return rabbitTemplate;
    }

    /**
     * 配置基础消息监听器容器工厂
     * 各服务可以调用此方法来创建具有统一配置的ListenerContainerFactory
     */
    protected SimpleRabbitListenerContainerFactory createBaseListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        
        // 配置预取数量
        factory.setPrefetchCount(10);
        
        // 配置并发消费者数量
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        
        // 配置手动确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        // 配置拒绝消息后不重新入队
        factory.setDefaultRequeueRejected(false);
        
        return factory;
    }
} 