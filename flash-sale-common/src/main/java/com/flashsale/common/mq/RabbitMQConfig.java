package com.flashsale.common.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Flash sale order queue
     */
    public static final String FLASH_SALE_ORDER_QUEUE = "flash.sale.order.queue";

    /**
     * Flash sale order delay queue
     */
    public static final String FLASH_SALE_ORDER_DELAY_QUEUE = "flash.sale.order.delay.queue";

    /**
     * Flash sale order exchange
     */
    public static final String FLASH_SALE_ORDER_EXCHANGE = "flash.sale.order.exchange";

    /**
     * Flash sale order delay exchange
     */
    public static final String FLASH_SALE_ORDER_DELAY_EXCHANGE = "flash.sale.order.delay.exchange";

    /**
     * Flash sale order routing key
     */
    public static final String FLASH_SALE_ORDER_ROUTING_KEY = "flash.sale.order.routing.key";

    /**
     * Flash sale order delay routing key
     */
    public static final String FLASH_SALE_ORDER_DELAY_ROUTING_KEY = "flash.sale.order.delay.routing.key";

    /**
     * Order TTL (milliseconds) - 30 minutes
     */
    public static final int ORDER_TTL = 30 * 60 * 1000;

    /**
     * Configure RabbitTemplate with Jackson2JsonMessageConverter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        
        // Enable publisher confirms and returns
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                // Handle nack
            }
        });
        
        rabbitTemplate.setReturnsCallback(returned -> {
            // Handle returned message
        });
        
        return rabbitTemplate;
    }

    /**
     * Configure RabbitListenerContainerFactory with Jackson2JsonMessageConverter
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        // Configure prefetch count
        factory.setPrefetchCount(1);
        // Configure concurrent consumers
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    /**
     * Flash sale order direct exchange
     */
    @Bean
    public DirectExchange flashSaleOrderExchange() {
        return new DirectExchange(FLASH_SALE_ORDER_EXCHANGE);
    }

    /**
     * Flash sale order queue
     */
    @Bean
    public Queue flashSaleOrderQueue() {
        return QueueBuilder.durable(FLASH_SALE_ORDER_QUEUE).build();
    }

    /**
     * Bind flash sale order queue to flash sale order exchange
     */
    @Bean
    public Binding flashSaleOrderBinding() {
        return BindingBuilder.bind(flashSaleOrderQueue())
                .to(flashSaleOrderExchange())
                .with(FLASH_SALE_ORDER_ROUTING_KEY);
    }

    /**
     * Flash sale order delay exchange (for order timeout)
     */
    @Bean
    public DirectExchange flashSaleOrderDelayExchange() {
        return new DirectExchange(FLASH_SALE_ORDER_DELAY_EXCHANGE);
    }

    /**
     * Flash sale order delay queue (for order timeout)
     */
    @Bean
    public Queue flashSaleOrderDelayQueue() {
        return QueueBuilder.durable(FLASH_SALE_ORDER_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", FLASH_SALE_ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", FLASH_SALE_ORDER_ROUTING_KEY)
                .withArgument("x-message-ttl", ORDER_TTL)
                .build();
    }

    /**
     * Bind flash sale order delay queue to flash sale order delay exchange
     */
    @Bean
    public Binding flashSaleOrderDelayBinding() {
        return BindingBuilder.bind(flashSaleOrderDelayQueue())
                .to(flashSaleOrderDelayExchange())
                .with(FLASH_SALE_ORDER_DELAY_ROUTING_KEY);
    }
} 