package com.flashsale.common.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 公平限流器 - 基于队列的先到先得限流机制
 * 确保用户按照到达顺序获得服务，避免限流破坏公平性
 * @author 21311
 */
@Slf4j
@Component
public class FairRateLimiter {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 队列前缀
    private static final String QUEUE_PREFIX = "fair_rate_limit:queue:";
    private static final String PROCESSING_PREFIX = "fair_rate_limit:processing:";
    private static final String USER_SEQUENCE_PREFIX = "fair_rate_limit:sequence:";
    private static final String SYSTEM_COUNTER_PREFIX = "fair_rate_limit:counter:";
    
    /**
     * 申请进入限流队列
     * @param userId 用户ID
     * @param resourceId 资源ID（如商品ID）
     * @param maxConcurrent 最大并发数
     * @param queueTimeout 队列超时时间（秒）
     * @return 排队令牌，null表示队列已满
     */
    public String requestQueueEntry(Long userId, Long resourceId, int maxConcurrent, int queueTimeout) {
        String queueKey = QUEUE_PREFIX + resourceId;
        String processingKey = PROCESSING_PREFIX + resourceId;
        String counterKey = SYSTEM_COUNTER_PREFIX + resourceId;
        
        // 生成全局序列号确保公平性
        Long sequence = redisTemplate.opsForValue().increment(counterKey);
        if (sequence == 1) {
            redisTemplate.expire(counterKey, queueTimeout * 2, TimeUnit.SECONDS);
        }
        
        String queueToken = userId + ":" + sequence + ":" + System.currentTimeMillis();
        
        // Lua脚本确保原子性操作
        String luaScript = 
            "local queueKey = KEYS[1] " +
            "local processingKey = KEYS[2] " +
            "local userSequenceKey = KEYS[3] " +
            "local queueToken = ARGV[1] " +
            "local userId = ARGV[2] " +
            "local maxConcurrent = tonumber(ARGV[3]) " +
            "local queueTimeout = tonumber(ARGV[4]) " +
            "local currentTime = tonumber(ARGV[5]) " +
            "local sequence = tonumber(ARGV[6]) " +
            
            // 检查用户是否已经在队列或处理中
            "local userInQueue = redis.call('zscore', queueKey, userId) " +
            "local userInProcessing = redis.call('sismember', processingKey, userId) " +
            "if userInQueue or userInProcessing == 1 then " +
            "    return nil " +
            "end " +
            
            // 检查当前处理中的数量
            "local processingCount = redis.call('scard', processingKey) " +
            "if processingCount < maxConcurrent then " +
            "    -- 直接进入处理状态 " +
            "    redis.call('sadd', processingKey, userId) " +
            "    redis.call('expire', processingKey, queueTimeout) " +
            "    redis.call('setex', userSequenceKey .. userId, queueTimeout, sequence) " +
            "    return 'IMMEDIATE:' .. queueToken " +
            "else " +
            "    -- 进入排队 " +
            "    redis.call('zadd', queueKey, sequence, userId) " +
            "    redis.call('expire', queueKey, queueTimeout) " +
            "    redis.call('setex', userSequenceKey .. userId, queueTimeout, sequence) " +
            "    return 'QUEUED:' .. queueToken " +
            "end";
        
        DefaultRedisScript<String> script = new DefaultRedisScript<>(luaScript, String.class);
        String userSequenceKey = USER_SEQUENCE_PREFIX + resourceId + ":";
        
        String result = redisTemplate.execute(script, 
            java.util.Arrays.asList(queueKey, processingKey, userSequenceKey),
            queueToken, userId.toString(), String.valueOf(maxConcurrent), 
            String.valueOf(queueTimeout), String.valueOf(System.currentTimeMillis()), 
            String.valueOf(sequence));
        
        if (result != null) {
            log.info("用户排队成功: userId={}, resourceId={}, result={}, sequence={}", 
                    userId, resourceId, result, sequence);
            return result;
        } else {
            log.warn("用户重复排队或队列满: userId={}, resourceId={}", userId, resourceId);
            return null;
        }
    }
    
    /**
     * 检查队列状态
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 状态信息：PROCESSING-处理中, QUEUED-排队中, NOT_FOUND-不在队列
     */
    public QueueStatus checkQueueStatus(Long userId, Long resourceId) {
        String queueKey = QUEUE_PREFIX + resourceId;
        String processingKey = PROCESSING_PREFIX + resourceId;
        String userSequenceKey = USER_SEQUENCE_PREFIX + resourceId + ":" + userId;
        
        // 检查是否在处理中
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(processingKey, userId))) {
            String sequence = (String) redisTemplate.opsForValue().get(userSequenceKey);
            return new QueueStatus("PROCESSING", 0, sequence != null ? Long.parseLong(sequence) : 0);
        }
        
        // 检查队列位置
        Double score = redisTemplate.opsForZSet().score(queueKey, userId);
        if (score != null) {
            Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
            return new QueueStatus("QUEUED", rank != null ? rank.intValue() + 1 : -1, score.longValue());
        }
        
        return new QueueStatus("NOT_FOUND", -1, 0);
    }
    
    /**
     * 完成处理，释放资源
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @param success 是否成功处理
     */
    public void completeProcessing(Long userId, Long resourceId, boolean success) {
        String queueKey = QUEUE_PREFIX + resourceId;
        String processingKey = PROCESSING_PREFIX + resourceId;
        String userSequenceKey = USER_SEQUENCE_PREFIX + resourceId + ":" + userId;
        
        // Lua脚本确保原子性
        String luaScript = 
            "local queueKey = KEYS[1] " +
            "local processingKey = KEYS[2] " +
            "local userSequenceKey = KEYS[3] " +
            "local userId = ARGV[1] " +
            "local success = ARGV[2] " +
            
            // 从处理集合中移除用户
            "redis.call('srem', processingKey, userId) " +
            "redis.call('del', userSequenceKey) " +
            
            // 如果有排队的用户，让下一个进入处理状态
            "local nextUser = redis.call('zrange', queueKey, 0, 0) " +
            "if #nextUser > 0 then " +
            "    local nextUserId = nextUser[1] " +
            "    redis.call('zrem', queueKey, nextUserId) " +
            "    redis.call('sadd', processingKey, nextUserId) " +
            "    return nextUserId " +
            "end " +
            "return nil";
        
        DefaultRedisScript<String> script = new DefaultRedisScript<>(luaScript, String.class);
        String nextUserId = redisTemplate.execute(script,
            java.util.Arrays.asList(queueKey, processingKey, userSequenceKey),
            userId.toString(), success ? "1" : "0");
        
        if (nextUserId != null) {
            log.info("队列轮转: 用户{}完成处理，用户{}进入处理状态, resourceId={}", 
                    userId, nextUserId, resourceId);
            // 这里可以发送通知给下一个用户
            notifyNextUser(Long.parseLong(nextUserId), resourceId);
        }
        
        log.info("用户完成处理: userId={}, resourceId={}, success={}", userId, resourceId, success);
    }
    
    /**
     * 强制清理过期的队列和处理状态
     * @param resourceId 资源ID
     */
    public void cleanupExpiredEntries(Long resourceId) {
        String queueKey = QUEUE_PREFIX + resourceId;
        String processingKey = PROCESSING_PREFIX + resourceId;
        
        // 清理过期的处理状态（超过5分钟未完成的）
        long expireTime = System.currentTimeMillis() - 300000; // 5分钟前
        
        // 这里可以添加更复杂的清理逻辑，比如检查用户最后活动时间
        log.info("清理过期队列条目: resourceId={}", resourceId);
    }
    
    /**
     * 通知下一个用户可以开始处理
     */
    private void notifyNextUser(Long userId, Long resourceId) {
        // 这里可以通过WebSocket、消息队列等方式通知用户
        // 暂时只记录日志
        log.info("通知用户可以开始处理: userId={}, resourceId={}", userId, resourceId);
    }
    
    /**
     * 队列状态封装类
     */
    public static class QueueStatus {
        private String status;
        private int position;
        private long sequence;
        
        public QueueStatus(String status, int position, long sequence) {
            this.status = status;
            this.position = position;
            this.sequence = sequence;
        }
        
        public String getStatus() { return status; }
        public int getPosition() { return position; }
        public long getSequence() { return sequence; }
        
        public boolean canProceed() {
            return "PROCESSING".equals(status);
        }
        
        @Override
        public String toString() {
            return String.format("QueueStatus{status='%s', position=%d, sequence=%d}", 
                                status, position, sequence);
        }
    }
} 