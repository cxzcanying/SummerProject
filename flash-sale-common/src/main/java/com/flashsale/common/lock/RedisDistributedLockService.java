package com.flashsale.common.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的分布式锁实现
 * @author 21311
 */
@Slf4j
@Service
public class RedisDistributedLockService implements DistributedLockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_PREFIX = "seckill:lock:";
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) " +
        "else return 0 end";
    
    // 存储当前线程持有的锁和对应的随机值
    private final ThreadLocal<ConcurrentHashMap<String, String>> lockHolder = 
        ThreadLocal.withInitial(ConcurrentHashMap::new);
    
    @Override
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        String fullLockKey = LOCK_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();
        
        long waitTimeMillis = timeUnit.toMillis(waitTime);
        long leaseTimeMillis = timeUnit.toMillis(leaseTime);
        long startTime = System.currentTimeMillis();
        
        while (System.currentTimeMillis() - startTime < waitTimeMillis) {
            Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(fullLockKey, lockValue, leaseTimeMillis, TimeUnit.MILLISECONDS);
            
            if (Boolean.TRUE.equals(result)) {
                lockHolder.get().put(fullLockKey, lockValue);
                log.debug("成功获取分布式锁: {}", lockKey);
                return true;
            }
            
            try {
                Thread.sleep(50); // 短暂等待后重试
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        
        log.warn("获取分布式锁超时: {}", lockKey);
        return false;
    }
    
    @Override
    public void unlock(String lockKey) {
        String fullLockKey = LOCK_PREFIX + lockKey;
        String lockValue = lockHolder.get().get(fullLockKey);
        
        if (lockValue != null) {
            try {
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptText(UNLOCK_SCRIPT);
                script.setResultType(Long.class);
                
                Long result = redisTemplate.execute(script, 
                    Collections.singletonList(fullLockKey), lockValue);
                
                if (result != null && result == 1) {
                    log.debug("成功释放分布式锁: {}", lockKey);
                } else {
                    log.warn("释放分布式锁失败，锁可能已过期: {}", lockKey);
                }
            } finally {
                lockHolder.get().remove(fullLockKey);
            }
        }
    }
    
    @Override
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, 
                                TimeUnit timeUnit, LockCallback<T> task) {
        if (tryLock(lockKey, waitTime, leaseTime, timeUnit)) {
            try {
                return task.execute();
            } catch (Exception e) {
                log.error("执行锁内业务逻辑失败: {}", lockKey, e);
                throw new RuntimeException("执行业务逻辑失败", e);
            } finally {
                unlock(lockKey);
            }
        } else {
            throw new RuntimeException("获取分布式锁失败: " + lockKey);
        }
    }
} 