package com.flashsale.common.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务
 * @author 21311
 */
@Component
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Set key-value with expiration time
     */
    public void set(String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * Set key-value without expiration time
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Get value by key
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Delete key
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * Delete multiple keys
     */
    public Long delete(List<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * Set expiration time
     */
    public Boolean expire(String key, long time) {
        return redisTemplate.expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * Get expiration time
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Check if key exists
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Increment by delta
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * Decrement by delta
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    /**
     * Hash operations - get
     */
    public Object hashGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash operations - put
     */
    public void hashPut(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash operations - put all
     */
    public void hashPutAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * Hash operations - delete
     */
    public void hashDelete(String key, Object... hashKeys) {
        redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * Set operations - add
     */
    public Long setAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Set operations - get
     */
    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * List operations - range
     */
    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * List operations - size
     */
    public Long listSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * List operations - left push
     */
    public Long listLeftPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List operations - right push
     */
    public Long listRightPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * Execute Lua script
     */
    public <T> T execute(DefaultRedisScript<T> script, List<String> keys, Object... args) {
        return redisTemplate.execute(script, keys, args);
    }

    /**
     * Execute Lua script with single key
     */
    public <T> T execute(DefaultRedisScript<T> script, String key, Object... args) {
        return execute(script, Collections.singletonList(key), args);
    }
} 