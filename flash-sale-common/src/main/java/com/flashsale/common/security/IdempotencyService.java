package com.flashsale.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 幂等性服务
 * @author 21311
 */
@Slf4j
@Service
public class IdempotencyService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final String SECKILL_PREFIX = "seckill:idempotency:";
    
    /**
     * 检查并设置幂等性标识
     * @param key 幂等性key
     * @param expireSeconds 过期时间（秒）
     * @return true-首次请求，false-重复请求
     */
    public boolean checkAndSetIdempotency(String key, long expireSeconds) {
        String fullKey = IDEMPOTENCY_PREFIX + key;
        
        // 使用 setIfAbsent 原子操作
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(fullKey, System.currentTimeMillis(), expireSeconds, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(result)) {
            log.debug("幂等性检查通过，首次请求: {}", key);
            return true;
        } else {
            log.warn("检测到重复请求: {}", key);
            return false;
        }
    }
    
    /**
     * 秒杀专用幂等性检查
     * @param userId 用户ID
     * @param productId 商品ID
     * @param requestId 请求ID
     * @return true-首次请求，false-重复请求
     */
    public boolean checkSeckillIdempotency(Long userId, Long productId, String requestId) {
        String key = SECKILL_PREFIX + userId + ":" + productId + ":" + requestId;
        // 秒杀请求5分钟内不允许重复
        return checkAndSetIdempotency(key, 300);
    }
    
    /**
     * 移除幂等性标识（用于回滚场景）
     * @param key 幂等性key
     */
    public void removeIdempotency(String key) {
        String fullKey = IDEMPOTENCY_PREFIX + key;
        redisTemplate.delete(fullKey);
        log.debug("移除幂等性标识: {}", key);
    }
    
    /**
     * 生成用户请求的幂等性key
     * @param userId 用户ID
     * @param action 操作类型
     * @param params 参数
     * @return 幂等性key
     */
    public String generateIdempotencyKey(Long userId, String action, String... params) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(userId).append(":").append(action);
        
        if (params != null && params.length > 0) {
            for (String param : params) {
                keyBuilder.append(":").append(param);
            }
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 检查幂等性是否存在
     * @param key 幂等性key
     * @return true-存在，false-不存在
     */
    public boolean isIdempotencyExists(String key) {
        String fullKey = IDEMPOTENCY_PREFIX + key;
        return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
    }
} 