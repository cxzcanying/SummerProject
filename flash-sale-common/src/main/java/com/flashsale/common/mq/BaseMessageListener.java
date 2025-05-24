package com.flashsale.common.mq;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 消息监听器基类，提供通用的消息处理逻辑
 * @author 21311
 */
@Slf4j
@Component
public abstract class BaseMessageListener {

    @Autowired
    private MessageSender messageSender;

    /**
     * 处理消息的抽象方法，由子类实现具体业务逻辑
     *
     * @param messageBody 消息体
     * @param message 原始消息
     * @param channel 通道
     * @return 处理结果
     */
    protected abstract boolean processMessage(Object messageBody, Message message, Channel channel);

    /**
     * 获取消息处理器名称，用于日志记录
     *
     * @return 处理器名称
     */
    protected abstract String getListenerName();

    /**
     * 统一的消息处理方法
     *
     * @param messageBody 消息体
     * @param message 原始消息
     * @param channel 通道
     */
    @RabbitHandler
    public void handleMessage(Object messageBody, Message message, Channel channel) {
        String correlationId = message.getMessageProperties().getCorrelationId();
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        log.info("[{}] 接收到消息，correlationId: {}, deliveryTag: {}", 
                getListenerName(), correlationId, deliveryTag);

        try {
            // 检查消息是否重复消费
            if (isDuplicateMessage(message)) {
                log.warn("[{}] 检测到重复消息，跳过处理，correlationId: {}", 
                        getListenerName(), correlationId);
                ackMessage(channel, deliveryTag);
                return;
            }

            // 处理消息
            boolean success = processMessage(messageBody, message, channel);
            
            if (success) {
                // 处理成功，确认消息
                ackMessage(channel, deliveryTag);
                log.info("[{}] 消息处理成功，correlationId: {}", getListenerName(), correlationId);
            } else {
                // 处理失败，进入重试逻辑
                handleProcessFailure(messageBody, message, channel, deliveryTag, "业务处理失败");
            }
            
        } catch (Exception e) {
            log.error("[{}] 消息处理异常，correlationId: {}", getListenerName(), correlationId, e);
            handleProcessFailure(messageBody, message, channel, deliveryTag, e.getMessage());
        }
    }

    /**
     * 处理消息处理失败的情况
     *
     * @param messageBody 消息体
     * @param message 原始消息
     * @param channel 通道
     * @param deliveryTag 投递标签
     * @param errorReason 错误原因
     */
    private void handleProcessFailure(Object messageBody, Message message, Channel channel, 
                                    long deliveryTag, String errorReason) {
        String correlationId = message.getMessageProperties().getCorrelationId();
        
        try {
            // 获取重试次数
            int retryCount = getRetryCount(message);
            
            if (retryCount < RabbitMQConfig.MAX_RETRY_COUNT) {
                // 还有重试机会，发送到重试队列
                retryMessage(messageBody, message, retryCount + 1, errorReason);
                ackMessage(channel, deliveryTag);
                log.warn("[{}] 消息处理失败，已重试 {}/{} 次，correlationId: {}, 错误: {}", 
                        getListenerName(), retryCount + 1, RabbitMQConfig.MAX_RETRY_COUNT, 
                        correlationId, errorReason);
            } else {
                // 重试次数已达上限，发送到死信队列
                sendToDeadLetterQueue(messageBody, errorReason, message);
                ackMessage(channel, deliveryTag);
                log.error("[{}] 消息处理失败，重试次数已达上限，发送到死信队列，correlationId: {}, 错误: {}", 
                        getListenerName(), correlationId, errorReason);
            }
        } catch (Exception e) {
            log.error("[{}] 处理失败消息时出现异常，correlationId: {}", getListenerName(), correlationId, e);
            // 拒绝消息并不重新入队
            nackMessage(channel, deliveryTag, false);
        }
    }

    /**
     * 检查是否为重复消息
     *
     * @param message 消息
     * @return 是否重复
     */
    private boolean isDuplicateMessage(Message message) {
        // 这里可以实现幂等性检查逻辑
        // 比如基于 correlationId 在 Redis 中检查是否已处理过
        // 简化实现，直接返回 false
        return false;
    }

    /**
     * 获取消息的重试次数
     *
     * @param message 消息
     * @return 重试次数
     */
    private int getRetryCount(Message message) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        if (headers != null && headers.containsKey("retryCount")) {
            return (Integer) headers.get("retryCount");
        }
        return 0;
    }

    /**
     * 重试消息
     *
     * @param messageBody 消息体
     * @param originalMessage 原始消息
     * @param retryCount 重试次数
     * @param errorReason 错误原因
     */
    private void retryMessage(Object messageBody, Message originalMessage, int retryCount, String errorReason) {
        try {
            // 获取原始的交换器和路由键，重新发送消息
            String exchange = originalMessage.getMessageProperties().getReceivedExchange();
            String routingKey = originalMessage.getMessageProperties().getReceivedRoutingKey();
            
            // 使用延迟发送，避免立即重试
            messageSender.sendMessageWithRetry(exchange, routingKey, messageBody, retryCount);
            
            log.info("[{}] 消息重试发送成功，retryCount: {}, errorReason: {}", 
                    getListenerName(), retryCount, errorReason);
        } catch (Exception e) {
            log.error("[{}] 消息重试发送失败，retryCount: {}", getListenerName(), retryCount, e);
            throw new RuntimeException("消息重试发送失败", e);
        }
    }

    /**
     * 发送消息到死信队列
     *
     * @param messageBody 消息体
     * @param errorReason 错误原因
     * @param originalMessage 原始消息
     */
    private void sendToDeadLetterQueue(Object messageBody, String errorReason, Message originalMessage) {
        try {
            messageSender.sendToDeadLetterQueue(messageBody, errorReason);
            log.warn("[{}] 消息已发送到死信队列，errorReason: {}", getListenerName(), errorReason);
        } catch (Exception e) {
            log.error("[{}] 发送消息到死信队列失败", getListenerName(), e);
        }
    }

    /**
     * 确认消息
     *
     * @param channel 通道
     * @param deliveryTag 投递标签
     */
    private void ackMessage(Channel channel, long deliveryTag) {
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("[{}] 确认消息失败，deliveryTag: {}", getListenerName(), deliveryTag, e);
        }
    }

    /**
     * 拒绝消息
     *
     * @param channel 通道
     * @param deliveryTag 投递标签
     * @param requeue 是否重新入队
     */
    private void nackMessage(Channel channel, long deliveryTag, boolean requeue) {
        try {
            channel.basicNack(deliveryTag, false, requeue);
        } catch (IOException e) {
            log.error("[{}] 拒绝消息失败，deliveryTag: {}", getListenerName(), deliveryTag, e);
        }
    }

    /**
     * 获取消息内容用于日志记录（子类可重写以控制日志输出）
     *
     * @param messageBody 消息体
     * @return 用于日志的消息内容
     */
    protected String getMessageForLog(Object messageBody) {
        if (messageBody == null) {
            return "null";
        }
        
        String messageStr = messageBody.toString();
        // 限制日志长度，避免日志过长
        if (messageStr.length() > 500) {
            return messageStr.substring(0, 500) + "...";
        }
        return messageStr;
    }

    /**
     * 检查消息是否有效（子类可重写以实现自定义验证）
     *
     * @param messageBody 消息体
     * @return 消息是否有效
     */
    protected boolean isValidMessage(Object messageBody) {
        return messageBody != null;
    }
} 