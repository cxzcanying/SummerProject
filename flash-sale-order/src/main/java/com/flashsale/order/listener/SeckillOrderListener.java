package com.flashsale.order.listener;

import com.flashsale.order.service.OrderService;
import com.flashsale.seckill.dto.SeckillDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单消息监听器
 * @author 21311
 */
@Slf4j
@Component
public class SeckillOrderListener {

    @Autowired
    private OrderService orderService;

    /**
     * 监听秒杀订单创建消息
     */
    @RabbitListener(queues = "flash.sale.order.queue")
    public void handleSeckillOrder(SeckillDTO seckillDTO) {
        log.info("收到秒杀订单创建消息：{}", seckillDTO);
        
        try {
            // 创建订单
            orderService.createSeckillOrder(seckillDTO);
            log.info("异步创建秒杀订单成功，用户ID：{}，商品ID：{}", 
                    seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        } catch (Exception e) {
            log.error("异步创建秒杀订单失败", e);
        }
    }
} 