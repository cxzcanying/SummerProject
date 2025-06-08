package com.flashsale.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 防黄牛服务
 * @author 21311
 */
@Slf4j
@Service
public class AntiScalpingService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String USER_REQUEST_COUNT_PREFIX = "anti_scalping:user_count:";
    private static final String IP_REQUEST_COUNT_PREFIX = "anti_scalping:ip_count:";
    private static final String DEVICE_REQUEST_COUNT_PREFIX = "anti_scalping:device_count:";
    private static final String USER_BEHAVIOR_PREFIX = "anti_scalping:behavior:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    /**
     * 检查用户是否为可疑黄牛
     * @param userId 用户ID
     * @param userIp 用户IP
     * @param deviceFingerprint 设备指纹
     * @param userLevel 用户等级
     * @param creditScore 信用分数
     * @param isVerified 是否实名认证
     * @return true-正常用户，false-可疑黄牛
     */
    public boolean checkUserLegitimacy(Long userId, String userIp, String deviceFingerprint, 
                                     Integer userLevel, Integer creditScore, Boolean isVerified) {
        // 1. 检查黑名单
        if (isInBlacklist(userId, userIp, deviceFingerprint)) {
            log.warn("用户在黑名单中: userId={}, ip={}", userId, userIp);
            return false;
        }
        
        // 2. 检查请求频率
        if (!checkRequestFrequency(userId, userIp, deviceFingerprint)) {
            log.warn("用户请求频率异常: userId={}, ip={}", userId, userIp);
            return false;
        }
        
        // 3. 检查用户信用度
        if (!checkUserCredibility(userLevel, creditScore, isVerified)) {
            log.warn("用户信用度不足: userId={}, level={}, score={}, verified={}", 
                    userId, userLevel, creditScore, isVerified);
            return false;
        }
        
        // 4. 检查行为模式
        if (!checkBehaviorPattern(userId)) {
            log.warn("用户行为模式异常: userId={}", userId);
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查黑名单
     */
    private boolean isInBlacklist(Long userId, String userIp, String deviceFingerprint) {
        String userKey = BLACKLIST_PREFIX + "user:" + userId;
        String ipKey = BLACKLIST_PREFIX + "ip:" + userIp;
        String deviceKey = BLACKLIST_PREFIX + "device:" + deviceFingerprint;
        
        return Boolean.TRUE.equals(redisTemplate.hasKey(userKey)) ||
               Boolean.TRUE.equals(redisTemplate.hasKey(ipKey)) ||
               Boolean.TRUE.equals(redisTemplate.hasKey(deviceKey));
    }
    
    /**
     * 检查请求频率
     */
    private boolean checkRequestFrequency(Long userId, String userIp, String deviceFingerprint) {
        // 用户维度：每分钟最多3次请求
        if (!checkAndIncrementCount(USER_REQUEST_COUNT_PREFIX + userId, 3, 60)) {
            return false;
        }
        
        // IP维度：每分钟最多10次请求
        if (!checkAndIncrementCount(IP_REQUEST_COUNT_PREFIX + userIp, 10, 60)) {
            return false;
        }
        
        // 设备维度：每分钟最多5次请求
        if (deviceFingerprint != null) {
            if (!checkAndIncrementCount(DEVICE_REQUEST_COUNT_PREFIX + deviceFingerprint, 5, 60)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查并递增计数
     */
    private boolean checkAndIncrementCount(String key, int maxCount, int windowSeconds) {
        Long currentCount = redisTemplate.opsForValue().increment(key);
        
        if (currentCount != null) {
            if (currentCount == 1) {
                // 首次访问，设置过期时间
                redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
            }
            
            return currentCount <= maxCount;
        }
        
        return true;
    }
    
    /**
     * 检查用户信用度
     */
    private boolean checkUserCredibility(Integer userLevel, Integer creditScore, Boolean isVerified) {
        // 未实名认证的用户信用度降低
        if (!Boolean.TRUE.equals(isVerified)) {
            return false;
        }
        
        // 信用分数过低
        if (creditScore != null && creditScore < 60) {
            return false;
        }
        
        // 新用户（等级过低）需要额外验证
        if (userLevel != null && userLevel < 2) {
            // 可以添加额外的新用户验证逻辑
            return creditScore != null && creditScore >= 80;
        }
        
        return true;
    }
    
    /**
     * 检查行为模式
     */
    private boolean checkBehaviorPattern(Long userId) {
        String behaviorKey = USER_BEHAVIOR_PREFIX + userId;
        
        // 记录用户行为时间戳
        Long currentTime = System.currentTimeMillis();
        redisTemplate.opsForList().leftPush(behaviorKey, currentTime);
        redisTemplate.expire(behaviorKey, 300, TimeUnit.SECONDS); // 保留5分钟
        
        // 获取最近的行为记录
        Long listSize = redisTemplate.opsForList().size(behaviorKey);
        if (listSize != null && listSize > 10) {
            // 如果5分钟内操作超过10次，可能是机器人
            return false;
        }
        
        return true;
    }
    
    /**
     * 添加到黑名单
     */
    public void addToBlacklist(Long userId, String userIp, String deviceFingerprint, 
                              String reason, int expireHours) {
        if (userId != null) {
            String userKey = BLACKLIST_PREFIX + "user:" + userId;
            redisTemplate.opsForValue().set(userKey, reason, expireHours, TimeUnit.HOURS);
        }
        
        if (userIp != null) {
            String ipKey = BLACKLIST_PREFIX + "ip:" + userIp;
            redisTemplate.opsForValue().set(ipKey, reason, expireHours, TimeUnit.HOURS);
        }
        
        if (deviceFingerprint != null) {
            String deviceKey = BLACKLIST_PREFIX + "device:" + deviceFingerprint;
            redisTemplate.opsForValue().set(deviceKey, reason, expireHours, TimeUnit.HOURS);
        }
        
        log.warn("添加到黑名单: userId={}, ip={}, device={}, reason={}", 
                userId, userIp, deviceFingerprint, reason);
    }
    
    /**
     * 移除黑名单
     */
    public void removeFromBlacklist(Long userId, String userIp, String deviceFingerprint) {
        if (userId != null) {
            redisTemplate.delete(BLACKLIST_PREFIX + "user:" + userId);
        }
        
        if (userIp != null) {
            redisTemplate.delete(BLACKLIST_PREFIX + "ip:" + userIp);
        }
        
        if (deviceFingerprint != null) {
            redisTemplate.delete(BLACKLIST_PREFIX + "device:" + deviceFingerprint);
        }
        
        log.info("从黑名单移除: userId={}, ip={}, device={}", userId, userIp, deviceFingerprint);
    }
    
    /**
     * 获取用户风险等级
     */
    public int getUserRiskLevel(Long userId, String userIp, Integer userLevel, 
                               Integer creditScore, Boolean isVerified) {
        int riskScore = 0;
        
        // 检查请求频率异常
        String userCountKey = USER_REQUEST_COUNT_PREFIX + userId;
        Object userCount = redisTemplate.opsForValue().get(userCountKey);
        if (userCount != null && (Integer) userCount > 2) {
            riskScore += 30;
        }
        
        // 检查IP频率异常
        String ipCountKey = IP_REQUEST_COUNT_PREFIX + userIp;
        Object ipCount = redisTemplate.opsForValue().get(ipCountKey);
        if (ipCount != null && (Integer) ipCount > 8) {
            riskScore += 40;
        }
        
        // 信用分数影响
        if (creditScore != null && creditScore < 70) {
            riskScore += 20;
        }
        
        // 实名认证影响
        if (!Boolean.TRUE.equals(isVerified)) {
            riskScore += 30;
        }
        
        // 用户等级影响
        if (userLevel != null && userLevel < 2) {
            riskScore += 10;
        }
        
        return Math.min(riskScore, 100); // 最高100分
    }
} 