package com.flashsale.seckill.service.impl;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.Result;
import com.flashsale.common.result.ResultCode;
import com.flashsale.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现类 - 已移除lua脚本
 * @author 21311
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Redis中秒杀商品的key前缀
     */
    private static final String SECKILL_PRODUCT_KEY = "seckill:product:";

    /**
     * Redis中用户秒杀记录的key前缀
     */
    private static final String USER_SECKILL_KEY = "user:seckill:";

    /**
     * 秒杀令牌的key前缀
     */
    private static final String SECKILL_TOKEN_KEY = "seckill:token:";

    /**
     * 秒杀令牌过期时间（秒）
     */
    private static final long TOKEN_EXPIRE_TIME = 300; // 5分钟

    @Override
    public Result<String> doSeckill(SeckillDTO seckillDTO) {
        Long userId = seckillDTO.getUserId();
        Long flashSaleProductId = seckillDTO.getFlashSaleProductId();
        Integer quantity = seckillDTO.getQuantity();

        try {
            // 1. 验证秒杀令牌
            Result<Boolean> tokenVerifyResult = verifySeckillToken(userId, flashSaleProductId, seckillDTO.getToken());
            if (!tokenVerifyResult.getData()) {
                return Result.error(ResultCode.SECKILL_FAILED.getCode(), "令牌验证失败");
            }

            // 2. 检查用户资格
            Result<Boolean> eligibilityResult = checkSeckillEligibility(userId, flashSaleProductId);
            if (!eligibilityResult.getData()) {
                return Result.error(ResultCode.SECKILL_REPEATED.getCode(), "您已参与过此秒杀活动");
            }

            // 3. 使用Redis原子操作扣减库存（替代lua脚本）
            String stockKey = SECKILL_PRODUCT_KEY + flashSaleProductId + ":stock";
            
            // 使用Redis的decrement操作，这是原子的
            Long remainingStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
            
            if (remainingStock == null) {
                return Result.error(ResultCode.PRODUCT_NOT_EXIST.getCode(), "商品不存在");
            }
            
            if (remainingStock < 0) {
                // 库存不足，回滚
                redisTemplate.opsForValue().increment(stockKey, quantity);
                return Result.error(ResultCode.PRODUCT_STOCK_NOT_ENOUGH.getCode(), "库存不足");
            }

            // 4. 记录用户秒杀
            String userSeckillKey = USER_SECKILL_KEY + userId + ":" + flashSaleProductId;
            redisTemplate.opsForValue().set(userSeckillKey, 1, 24, TimeUnit.HOURS);

            // 5. 生成订单号
            String orderNo = generateOrderNo();

            // 6. 发送消息到订单服务创建订单
            seckillDTO.setToken(orderNo); // 使用orderNo作为消息标识
            rabbitTemplate.convertAndSend("flash.sale.order.exchange", "flash.sale.order.routing.key", seckillDTO);

            log.info("用户{}成功秒杀商品{}，订单号：{}", userId, flashSaleProductId, orderNo);
            return Result.success(orderNo);

        } catch (Exception e) {
            log.error("秒杀失败", e);
            return Result.error(ResultCode.SECKILL_FAILED.getCode(), "秒杀失败");
        }
    }

    @Override
    public Result<Boolean> checkSeckillEligibility(Long userId, Long flashSaleProductId) {
        String userSeckillKey = USER_SECKILL_KEY + userId + ":" + flashSaleProductId;
        Boolean hasParticipated = redisTemplate.hasKey(userSeckillKey);
        return Result.success(Boolean.FALSE.equals(hasParticipated));
    }

    @Override
    public Result<Void> preloadSeckillProducts(Long activityId) {
        try {
            // 这里需要调用商品服务获取秒杀商品列表
            // 然后将库存信息预热到Redis中
            // 简化实现，实际应该通过Feign调用商品服务
            log.info("预热活动{}的秒杀商品到Redis", activityId);
            return Result.success();
        } catch (Exception e) {
            log.error("预热秒杀商品失败", e);
            return Result.error(ResultCode.ERROR.getCode(), "预热失败");
        }
    }

    @Override
    public Result<Integer> getSeckillStock(Long flashSaleProductId) {
        String stockKey = SECKILL_PRODUCT_KEY + flashSaleProductId + ":stock";
        Object stock = redisTemplate.opsForValue().get(stockKey);
        if (stock == null) {
            return Result.success(0);
        }
        return Result.success(Integer.parseInt(stock.toString()));
    }

    @Override
    public Result<String> generateSeckillToken(Long userId, Long flashSaleProductId) {
        // 生成令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = SECKILL_TOKEN_KEY + userId + ":" + flashSaleProductId;
        
        // 将令牌存储到Redis，设置过期时间
        redisTemplate.opsForValue().set(tokenKey, token, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        
        return Result.success(token);
    }

    @Override
    public Result<Boolean> verifySeckillToken(Long userId, Long flashSaleProductId, String token) {
        String tokenKey = SECKILL_TOKEN_KEY + userId + ":" + flashSaleProductId;
        Object storedToken = redisTemplate.opsForValue().get(tokenKey);
        
        if (storedToken == null) {
            return Result.success(false);
        }
        
        boolean isValid = token.equals(storedToken.toString());
        
        // 验证后删除令牌（一次性使用）
        if (isValid) {
            redisTemplate.delete(tokenKey);
        }
        
        return Result.success(isValid);
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "FS" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
} 