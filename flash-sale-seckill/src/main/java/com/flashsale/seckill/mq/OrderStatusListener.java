package com.flashsale.seckill.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashsale.common.mq.RabbitMQConfig;
import com.flashsale.seckill.mapper.SeckillOrderMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 订单状态更新监听器
 * @author 21311
 */
@Slf4j
@Component
public class OrderStatusListener {

    @Autowired
    private SeckillOrderMapper orderMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String SECKILL_RESULT_KEY = "seckill:result:";

    @RabbitListener(queues = RabbitMQConfig.ORDER_STATUS_UPDATE_QUEUE,
                    containerFactory = "seckillRabbitListenerContainerFactory")
    public void onOrderStatusUpdate(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            log.info("收到订单状态更新消息");
            
            // 获取消息内容
            String messageContent = new String(message.getBody());
            log.info("订单状态更新消息内容: {}", messageContent);
            
            // 解析JSON
            JsonNode jsonNode = objectMapper.readTree(messageContent);
            
            // 提取必要字段
            String orderNo = jsonNode.has("orderNo") ? jsonNode.get("orderNo").asText() : null;
            Integer newStatus = jsonNode.has("status") ? jsonNode.get("status").asInt() : null;
            String paymentId = jsonNode.has("paymentId") ? jsonNode.get("paymentId").asText() : null;
            
            // 验证必要字段
            if (orderNo == null || newStatus == null) {
                log.error("订单状态更新消息缺少必要字段: orderNo={}, status={}", orderNo, newStatus);
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            
            log.info("解析成功: 订单号={}, 新状态={}, 支付ID={}", orderNo, newStatus, paymentId);
            
            // 更新订单状态
            int updateResult = orderMapper.updateStatusByOrderNo(orderNo, newStatus);
            if (updateResult > 0) {
                log.info("订单状态更新成功，订单号: {}, 状态: {}", orderNo, newStatus);
                
                // 更新Redis中的秒杀结果
                String status = switch (newStatus) {
                    case 1 -> "PAID";
                    // 已支付
                    case 2 -> "SHIPPED";
                    // 已发货
                    case 3 -> "COMPLETED";
                    // 已完成
                    case 4 -> "CANCELLED";
                    // 已取消
                    case 5 -> "TIMEOUT";
                    // 已超时
                    default -> "UNKNOWN";
                };
                
                redisTemplate.opsForValue().set(SECKILL_RESULT_KEY + orderNo, status, 24, TimeUnit.HOURS);
                log.info("更新秒杀结果缓存: 订单号={}, 状态={}", orderNo, status);
            } else {
                log.warn("订单状态更新失败，可能订单不存在: {}", orderNo);
            }
            
            // 确认消息
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            log.error("处理订单状态更新失败: {}", e.getMessage(), e);
            
            // 拒绝消息并不重新入队（避免无限循环）
            channel.basicNack(deliveryTag, false, false);
        }
    }
} 