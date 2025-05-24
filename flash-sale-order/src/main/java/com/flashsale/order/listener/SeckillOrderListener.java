package com.flashsale.order.listener;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 21311
 */
@Slf4j
@Component
public class SeckillOrderListener {
    
    @Autowired
    private OrderService orderService;

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