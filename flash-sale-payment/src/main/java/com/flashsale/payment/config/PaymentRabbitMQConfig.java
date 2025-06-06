package com.flashsale.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flashsale.common.mq.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 支付服务RabbitMQ配置类 - 继承基础配置，添加支付服务特有配置
 * @author 21311
 */
@Configuration
@EnableRabbit
public class PaymentRabbitMQConfig extends RabbitMQConfig {

    /**
     * 支付服务专用的ObjectMapper配置
     */
    @Bean("paymentObjectMapper")
    public ObjectMapper paymentObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * 支付服务专用的JSON消息转换器
     */
    @Bean("paymentJsonMessageConverter")
    public MessageConverter paymentJsonMessageConverter() {
        return new Jackson2JsonMessageConverter(paymentObjectMapper());
    }
    
    /**
     * 支付服务专用的RabbitTemplate
     * 使用基础配置方法，确保具有完整的发送确认和回调机制
     */
    @Bean("paymentRabbitTemplate")
    public RabbitTemplate paymentRabbitTemplate(ConnectionFactory connectionFactory) {
        return createBaseRabbitTemplate(connectionFactory, 
                (Jackson2JsonMessageConverter) paymentJsonMessageConverter());
    }
    
    /**
     * 支付服务专用的消息监听器容器工厂
     * 使用基础配置方法，确保具有统一的消费者配置
     */
    @Bean("paymentRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory paymentRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        return createBaseListenerContainerFactory(connectionFactory, 
                (Jackson2JsonMessageConverter) paymentJsonMessageConverter());
    }
} 