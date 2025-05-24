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
     * 秒杀主题交换机 - 用于秒杀相关消息路由
     */
    public static final String SECKILL_TOPIC_EXCHANGE = "seckill.topic.exchange";

    /**
     * 库存操作交换机 - 用于库存相关操作
     */
    public static final String STOCK_EXCHANGE = "stock.exchange";

    /**
     * 订单操作交换机 - 用于订单相关操作
     */
    public static final String ORDER_EXCHANGE = "order.exchange";

    /**
     * 支付操作交换机 - 用于支付相关操作
     */
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    /**
     * 日志记录交换机 - 用于日志收集
     */
    public static final String LOG_EXCHANGE = "log.exchange";

    /**
     * 通知推送交换机 - 用于消息通知
     */
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    /**
     * 延迟处理交换机 - 用于延迟任务
     */
    public static final String DELAY_EXCHANGE = "delay.exchange";

    /**
     * 死信交换机 - 用于异常消息处理
     */
    public static final String DLX_EXCHANGE = "dlx.exchange";

    // ==================== 队列名称常量 ====================
    
    /**
     * 秒杀请求队列 - 流量削峰
     */
    public static final String SECKILL_REQUEST_QUEUE = "seckill.request.queue";

    /**
     * 库存扣减队列
     */
    public static final String STOCK_DEDUCT_QUEUE = "stock.deduct.queue";

    /**
     * 库存回滚队列
     */
    public static final String STOCK_ROLLBACK_QUEUE = "stock.rollback.queue";

    /**
     * 库存同步队列
     */
    public static final String STOCK_SYNC_QUEUE = "stock.sync.queue";

    /**
     * 订单创建队列
     */
    public static final String ORDER_CREATE_QUEUE = "order.create.queue";

    /**
     * 订单取消队列
     */
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";

    /**
     * 订单状态更新队列
     */
    public static final String ORDER_STATUS_UPDATE_QUEUE = "order.status.update.queue";

    /**
     * 支付处理队列
     */
    public static final String PAYMENT_PROCESS_QUEUE = "payment.process.queue";

    /**
     * 支付回调队列
     */
    public static final String PAYMENT_CALLBACK_QUEUE = "payment.callback.queue";

    /**
     * 支付退款队列
     */
    public static final String PAYMENT_REFUND_QUEUE = "payment.refund.queue";

    /**
     * 业务日志队列
     */
    public static final String BUSINESS_LOG_QUEUE = "business.log.queue";

    /**
     * 操作日志队列
     */
    public static final String OPERATION_LOG_QUEUE = "operation.log.queue";

    /**
     * 错误日志队列
     */
    public static final String ERROR_LOG_QUEUE = "error.log.queue";

    /**
     * 短信通知队列
     */
    public static final String SMS_NOTIFICATION_QUEUE = "sms.notification.queue";

    /**
     * 邮件通知队列
     */
    public static final String EMAIL_NOTIFICATION_QUEUE = "email.notification.queue";

    /**
     * 推送通知队列
     */
    public static final String PUSH_NOTIFICATION_QUEUE = "push.notification.queue";

    /**
     * 延迟订单处理队列
     */
    public static final String DELAY_ORDER_QUEUE = "delay.order.queue";

    /**
     * 延迟库存检查队列
     */
    public static final String DELAY_STOCK_CHECK_QUEUE = "delay.stock.check.queue";

    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE = "dead.letter.queue";

    // ==================== 路由键常量 ====================
    
    /**
     * 秒杀请求路由键
     */
    public static final String SECKILL_REQUEST_ROUTING_KEY = "seckill.request";

    /**
     * 秒杀成功路由键
     */
    public static final String SECKILL_SUCCESS_ROUTING_KEY = "seckill.success";

    /**
     * 秒杀失败路由键
     */
    public static final String SECKILL_FAILED_ROUTING_KEY = "seckill.failed";

    /**
     * 库存扣减路由键
     */
    public static final String STOCK_DEDUCT_ROUTING_KEY = "stock.deduct";

    /**
     * 库存回滚路由键
     */
    public static final String STOCK_ROLLBACK_ROUTING_KEY = "stock.rollback";

    /**
     * 库存同步路由键
     */
    public static final String STOCK_SYNC_ROUTING_KEY = "stock.sync";

    /**
     * 订单创建路由键
     */
    public static final String ORDER_CREATE_ROUTING_KEY = "order.create";

    /**
     * 订单取消路由键
     */
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    /**
     * 订单状态更新路由键
     */
    public static final String ORDER_STATUS_UPDATE_ROUTING_KEY = "order.status.update";

    /**
     * 支付处理路由键
     */
    public static final String PAYMENT_PROCESS_ROUTING_KEY = "payment.process";

    /**
     * 支付回调路由键
     */
    public static final String PAYMENT_CALLBACK_ROUTING_KEY = "payment.callback";

    /**
     * 支付退款路由键
     */
    public static final String PAYMENT_REFUND_ROUTING_KEY = "payment.refund";

    /**
     * 业务日志路由键
     */
    public static final String BUSINESS_LOG_ROUTING_KEY = "log.business";

    /**
     * 操作日志路由键
     */
    public static final String OPERATION_LOG_ROUTING_KEY = "log.operation";

    /**
     * 错误日志路由键
     */
    public static final String ERROR_LOG_ROUTING_KEY = "log.error";

    /**
     * 短信通知路由键
     */
    public static final String SMS_NOTIFICATION_ROUTING_KEY = "notification.sms";

    /**
     * 邮件通知路由键
     */
    public static final String EMAIL_NOTIFICATION_ROUTING_KEY = "notification.email";

    /**
     * 推送通知路由键
     */
    public static final String PUSH_NOTIFICATION_ROUTING_KEY = "notification.push";

    /**
     * 延迟订单路由键
     */
    public static final String DELAY_ORDER_ROUTING_KEY = "delay.order";

    /**
     * 延迟库存检查路由键
     */
    public static final String DELAY_STOCK_CHECK_ROUTING_KEY = "delay.stock.check";

    /**
     * 死信路由键
     */
    public static final String DEAD_LETTER_ROUTING_KEY = "dead.letter";

    // ==================== TTL常量 ====================
    
    /**
     * 订单超时时间 - 30分钟
     */
    public static final int ORDER_TTL = 30 * 60 * 1000;

    /**
     * 库存检查延迟时间 - 5分钟
     */
    public static final int STOCK_CHECK_DELAY = 5 * 60 * 1000;

    /**
     * 消息重试TTL - 10秒
     */
    public static final int RETRY_TTL = 10 * 1000;

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
        
        // 开启发送方确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("消息发送失败，correlationData: {}, cause: {}", correlationData, cause);
                // 可以在这里实现重试机制或记录失败消息
            } else {
                log.debug("消息发送成功，correlationData: {}", correlationData);
            }
        });
        
        // 开启发送方回调
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("消息投递失败，returned message: {}, replyCode: {}, replyText: {}, exchange: {}, routingKey: {}",
                    returned.getMessage(), returned.getReplyCode(), returned.getReplyText(),
                    returned.getExchange(), returned.getRoutingKey());
            // 可以在这里实现消息补偿机制
        });
        
        // 设置mandatory为true，当消息无法路由到队列时会触发return callback
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
        
        // 配置预取数量（每个消费者预取的消息数量）
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

    // ==================== 交换机配置 ====================
    
    /**
     * 秒杀主题交换机
     */
    @Bean
    public TopicExchange seckillTopicExchange() {
        return ExchangeBuilder.topicExchange(SECKILL_TOPIC_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 库存操作直连交换机
     */
    @Bean
    public DirectExchange stockExchange() {
        return ExchangeBuilder.directExchange(STOCK_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 订单操作直连交换机
     */
    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 支付操作直连交换机
     */
    @Bean
    public DirectExchange paymentExchange() {
        return ExchangeBuilder.directExchange(PAYMENT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 日志记录主题交换机
     */
    @Bean
    public TopicExchange logExchange() {
        return ExchangeBuilder.topicExchange(LOG_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 通知推送主题交换机
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 延迟处理直连交换机
     */
    @Bean
    public DirectExchange delayExchange() {
        return ExchangeBuilder.directExchange(DELAY_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 秒杀相关队列配置 ====================
    
    /**
     * 秒杀请求队列 - 用于流量削峰
     */
    @Bean
    public Queue seckillRequestQueue() {
        return QueueBuilder.durable(SECKILL_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定秒杀请求队列
     */
    @Bean
    public Binding seckillRequestBinding() {
        return BindingBuilder.bind(seckillRequestQueue())
                .to(seckillTopicExchange())
                .with(SECKILL_REQUEST_ROUTING_KEY);
    }

    // ==================== 库存相关队列配置 ====================
    
    /**
     * 库存扣减队列
     */
    @Bean
    public Queue stockDeductQueue() {
        return QueueBuilder.durable(STOCK_DEDUCT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 库存回滚队列
     */
    @Bean
    public Queue stockRollbackQueue() {
        return QueueBuilder.durable(STOCK_ROLLBACK_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 库存同步队列
     */
    @Bean
    public Queue stockSyncQueue() {
        return QueueBuilder.durable(STOCK_SYNC_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定库存相关队列
     */
    @Bean
    public Binding stockDeductBinding() {
        return BindingBuilder.bind(stockDeductQueue())
                .to(stockExchange())
                .with(STOCK_DEDUCT_ROUTING_KEY);
    }

    @Bean
    public Binding stockRollbackBinding() {
        return BindingBuilder.bind(stockRollbackQueue())
                .to(stockExchange())
                .with(STOCK_ROLLBACK_ROUTING_KEY);
    }

    @Bean
    public Binding stockSyncBinding() {
        return BindingBuilder.bind(stockSyncQueue())
                .to(stockExchange())
                .with(STOCK_SYNC_ROUTING_KEY);
    }

    // ==================== 订单相关队列配置 ====================
    
    /**
     * 订单创建队列
     */
    @Bean
    public Queue orderCreateQueue() {
        return QueueBuilder.durable(ORDER_CREATE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 订单取消队列
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 订单状态更新队列
     */
    @Bean
    public Queue orderStatusUpdateQueue() {
        return QueueBuilder.durable(ORDER_STATUS_UPDATE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定订单相关队列
     */
    @Bean
    public Binding orderCreateBinding() {
        return BindingBuilder.bind(orderCreateQueue())
                .to(orderExchange())
                .with(ORDER_CREATE_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue())
                .to(orderExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }

    @Bean
    public Binding orderStatusUpdateBinding() {
        return BindingBuilder.bind(orderStatusUpdateQueue())
                .to(orderExchange())
                .with(ORDER_STATUS_UPDATE_ROUTING_KEY);
    }

    // ==================== 支付相关队列配置 ====================
    
    /**
     * 支付处理队列
     */
    @Bean
    public Queue paymentProcessQueue() {
        return QueueBuilder.durable(PAYMENT_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 支付回调队列
     */
    @Bean
    public Queue paymentCallbackQueue() {
        return QueueBuilder.durable(PAYMENT_CALLBACK_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 支付退款队列
     */
    @Bean
    public Queue paymentRefundQueue() {
        return QueueBuilder.durable(PAYMENT_REFUND_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定支付相关队列
     */
    @Bean
    public Binding paymentProcessBinding() {
        return BindingBuilder.bind(paymentProcessQueue())
                .to(paymentExchange())
                .with(PAYMENT_PROCESS_ROUTING_KEY);
    }

    @Bean
    public Binding paymentCallbackBinding() {
        return BindingBuilder.bind(paymentCallbackQueue())
                .to(paymentExchange())
                .with(PAYMENT_CALLBACK_ROUTING_KEY);
    }

    @Bean
    public Binding paymentRefundBinding() {
        return BindingBuilder.bind(paymentRefundQueue())
                .to(paymentExchange())
                .with(PAYMENT_REFUND_ROUTING_KEY);
    }

    // ==================== 日志相关队列配置 ====================
    
    /**
     * 业务日志队列
     */
    @Bean
    public Queue businessLogQueue() {
        return QueueBuilder.durable(BUSINESS_LOG_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 操作日志队列
     */
    @Bean
    public Queue operationLogQueue() {
        return QueueBuilder.durable(OPERATION_LOG_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 错误日志队列
     */
    @Bean
    public Queue errorLogQueue() {
        return QueueBuilder.durable(ERROR_LOG_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定日志相关队列
     */
    @Bean
    public Binding businessLogBinding() {
        return BindingBuilder.bind(businessLogQueue())
                .to(logExchange())
                .with(BUSINESS_LOG_ROUTING_KEY);
    }

    @Bean
    public Binding operationLogBinding() {
        return BindingBuilder.bind(operationLogQueue())
                .to(logExchange())
                .with(OPERATION_LOG_ROUTING_KEY);
    }

    @Bean
    public Binding errorLogBinding() {
        return BindingBuilder.bind(errorLogQueue())
                .to(logExchange())
                .with(ERROR_LOG_ROUTING_KEY);
    }

    // ==================== 通知相关队列配置 ====================
    
    /**
     * 短信通知队列
     */
    @Bean
    public Queue smsNotificationQueue() {
        return QueueBuilder.durable(SMS_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 邮件通知队列
     */
    @Bean
    public Queue emailNotificationQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 推送通知队列
     */
    @Bean
    public Queue pushNotificationQueue() {
        return QueueBuilder.durable(PUSH_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * 绑定通知相关队列
     */
    @Bean
    public Binding smsNotificationBinding() {
        return BindingBuilder.bind(smsNotificationQueue())
                .to(notificationExchange())
                .with(SMS_NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding emailNotificationBinding() {
        return BindingBuilder.bind(emailNotificationQueue())
                .to(notificationExchange())
                .with(EMAIL_NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding pushNotificationBinding() {
        return BindingBuilder.bind(pushNotificationQueue())
                .to(notificationExchange())
                .with(PUSH_NOTIFICATION_ROUTING_KEY);
    }

    // ==================== 延迟相关队列配置 ====================
    
    /**
     * 延迟订单处理队列
     */
    @Bean
    public Queue delayOrderQueue() {
        return QueueBuilder.durable(DELAY_ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_CANCEL_ROUTING_KEY)
                .withArgument("x-message-ttl", ORDER_TTL)
                .build();
    }

    /**
     * 延迟库存检查队列
     */
    @Bean
    public Queue delayStockCheckQueue() {
        return QueueBuilder.durable(DELAY_STOCK_CHECK_QUEUE)
                .withArgument("x-dead-letter-exchange", STOCK_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", STOCK_SYNC_ROUTING_KEY)
                .withArgument("x-message-ttl", STOCK_CHECK_DELAY)
                .build();
    }

    /**
     * 绑定延迟相关队列
     */
    @Bean
    public Binding delayOrderBinding() {
        return BindingBuilder.bind(delayOrderQueue())
                .to(delayExchange())
                .with(DELAY_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding delayStockCheckBinding() {
        return BindingBuilder.bind(delayStockCheckQueue())
                .to(delayExchange())
                .with(DELAY_STOCK_CHECK_ROUTING_KEY);
    }

    // ==================== 死信队列配置 ====================
    
    /**
     * 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    /**
     * 绑定死信队列
     */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(dlxExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }
} 