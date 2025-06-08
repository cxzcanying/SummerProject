package com.flashsale.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * 增强版令牌服务
 * @author 21311
 */
@Slf4j
@Service
public class EnhancedTokenService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private AntiScalpingService antiScalpingService;
    
    private static final String TOKEN_PREFIX = "enhanced:token:";
    private static final String TOKEN_USAGE_PREFIX = "token:usage:";
    private static final String TOKEN_GENERATE_COUNT_PREFIX = "token:generate:count:";
    
    /**
     * 生成增强版秒杀令牌
     * @param userId 用户ID
     * @param productId 商品ID
     * @param userIp 用户IP
     * @param deviceFingerprint 设备指纹
     * @param userLevel 用户等级
     * @param creditScore 信用分数
     * @param isVerified 是否实名认证
     * @param challengeAnswer 挑战题答案（验证码/滑块验证等）
     * @return 令牌字符串，失败返回null
     */
    public String generateEnhancedToken(Long userId, Long productId, String userIp, 
                                      String deviceFingerprint, Integer userLevel, 
                                      Integer creditScore, Boolean isVerified, 
                                      String challengeAnswer) {
        
        // 1. 防黄牛检查
        if (!antiScalpingService.checkUserLegitimacy(userId, userIp, deviceFingerprint, 
                userLevel, creditScore, isVerified)) {
            log.warn("用户未通过防黄牛检查: userId={}", userId);
            return null;
        }
        
        // 2. 检查令牌生成频率
        if (!checkTokenGenerateFrequency(userId, userIp)) {
            log.warn("令牌生成频率过高: userId={}, ip={}", userId, userIp);
            return null;
        }
        
        // 3. 验证挑战题答案（这里简化处理，实际应该验证验证码等）
        if (!validateChallengeAnswer(userId, challengeAnswer)) {
            log.warn("挑战题验证失败: userId={}", userId);
            return null;
        }
        
        // 4. 计算用户权重（VIP用户获得更长有效期的令牌）
        int tokenValidityMinutes = calculateTokenValidity(userLevel, creditScore, isVerified);
        
        // 5. 生成令牌
        String token = generateSecureToken(userId, productId, userIp, System.currentTimeMillis());
        
        // 6. 存储令牌信息
        String tokenKey = TOKEN_PREFIX + userId + ":" + productId;
        TokenInfo tokenInfo = new TokenInfo(token, userId, productId, userIp, 
                deviceFingerprint, System.currentTimeMillis(), false);
        
        redisTemplate.opsForValue().set(tokenKey, tokenInfo, tokenValidityMinutes, TimeUnit.MINUTES);
        
        // 7. 记录令牌生成次数
        recordTokenGeneration(userId, userIp);
        
        log.info("成功生成增强令牌: userId={}, productId={}, validity={}分钟", 
                userId, productId, tokenValidityMinutes);
        
        return token;
    }
    
    /**
     * 验证并消费令牌
     * @param userId 用户ID
     * @param productId 商品ID
     * @param token 令牌
     * @param userIp 用户IP
     * @return true-验证成功，false-验证失败
     */
    public boolean validateAndConsumeToken(Long userId, Long productId, String token, String userIp) {
        String tokenKey = TOKEN_PREFIX + userId + ":" + productId;
        
        Object tokenObj = redisTemplate.opsForValue().get(tokenKey);
        if (tokenObj == null) {
            log.warn("令牌不存在或已过期: userId={}, productId={}", userId, productId);
            return false;
        }
        
        TokenInfo tokenInfo = (TokenInfo) tokenObj;
        
        // 验证令牌是否匹配
        if (!token.equals(tokenInfo.getToken())) {
            log.warn("令牌不匹配: userId={}, productId={}", userId, productId);
            return false;
        }
        
        // 验证IP是否匹配（防止令牌被盗用）
        if (!userIp.equals(tokenInfo.getIp())) {
            log.warn("IP不匹配，疑似令牌被盗用: userId={}, originalIp={}, currentIp={}", 
                    userId, tokenInfo.getIp(), userIp);
            return false;
        }
        
        // 检查令牌是否已被使用
        if (tokenInfo.isUsed()) {
            log.warn("令牌已被使用: userId={}, productId={}", userId, productId);
            return false;
        }
        
        // 标记令牌为已使用
        tokenInfo.setUsed(true);
        redisTemplate.opsForValue().set(tokenKey, tokenInfo, 5, TimeUnit.MINUTES);
        
        // 记录令牌使用
        recordTokenUsage(userId, productId, token);
        
        log.info("令牌验证成功并已消费: userId={}, productId={}", userId, productId);
        return true;
    }
    
    /**
     * 检查令牌生成频率
     */
    private boolean checkTokenGenerateFrequency(Long userId, String userIp) {
        // 用户维度：每小时最多生成5个令牌
        String userCountKey = TOKEN_GENERATE_COUNT_PREFIX + "user:" + userId;
        Long userCount = redisTemplate.opsForValue().increment(userCountKey);
        if (userCount == 1) {
            redisTemplate.expire(userCountKey, 1, TimeUnit.HOURS);
        }
        if (userCount > 5) {
            return false;
        }
        
        // IP维度：每小时最多生成20个令牌
        String ipCountKey = TOKEN_GENERATE_COUNT_PREFIX + "ip:" + userIp;
        Long ipCount = redisTemplate.opsForValue().increment(ipCountKey);
        if (ipCount == 1) {
            redisTemplate.expire(ipCountKey, 1, TimeUnit.HOURS);
        }
        if (ipCount > 20) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证挑战题答案
     */
    private boolean validateChallengeAnswer(Long userId, String challengeAnswer) {
        // 这里简化处理，实际应该验证验证码、滑块验证等
        // 可以从Redis中获取之前存储的验证码进行对比
        return challengeAnswer != null && challengeAnswer.length() >= 4;
    }
    
    /**
     * 计算令牌有效期
     */
    private int calculateTokenValidity(Integer userLevel, Integer creditScore, Boolean isVerified) {
        int baseValidity = 10; // 基础10分钟
        
        // VIP用户延长有效期
        if (userLevel != null && userLevel >= 3) {
            baseValidity += 10;
        } else if (userLevel != null && userLevel >= 2) {
            baseValidity += 5;
        }
        
        // 高信用分数用户延长有效期
        if (creditScore != null && creditScore >= 90) {
            baseValidity += 5;
        } else if (creditScore != null && creditScore >= 80) {
            baseValidity += 3;
        }
        
        // 实名认证用户延长有效期
        if (Boolean.TRUE.equals(isVerified)) {
            baseValidity += 5;
        }
        
        return Math.min(baseValidity, 30); // 最长30分钟
    }
    
    /**
     * 生成安全令牌
     */
    private String generateSecureToken(Long userId, Long productId, String userIp, long timestamp) {
        try {
            String input = userId + ":" + productId + ":" + userIp + ":" + timestamp + ":SECKILL_SECRET";
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32); // 取前32位
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("生成令牌失败", e);
        }
    }
    
    /**
     * 记录令牌生成
     */
    private void recordTokenGeneration(Long userId, String userIp) {
        // 可以记录到数据库或者日志系统，用于后续分析
        log.info("令牌生成记录: userId={}, ip={}, time={}", userId, userIp, System.currentTimeMillis());
    }
    
    /**
     * 记录令牌使用
     */
    private void recordTokenUsage(Long userId, Long productId, String token) {
        String usageKey = TOKEN_USAGE_PREFIX + userId + ":" + productId;
        redisTemplate.opsForValue().set(usageKey, token, 1, TimeUnit.DAYS);
        log.info("令牌使用记录: userId={}, productId={}, time={}", userId, productId, System.currentTimeMillis());
    }
    
    /**
     * 令牌信息类
     */
    public static class TokenInfo {
        private String token;
        private Long userId;
        private Long productId;
        private String ip;
        private String deviceFingerprint;
        private long createTime;
        private boolean used;
        
        public TokenInfo() {}
        
        public TokenInfo(String token, Long userId, Long productId, String ip, 
                        String deviceFingerprint, long createTime, boolean used) {
            this.token = token;
            this.userId = userId;
            this.productId = productId;
            this.ip = ip;
            this.deviceFingerprint = deviceFingerprint;
            this.createTime = createTime;
            this.used = used;
        }
        
        // Getters and Setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        public String getDeviceFingerprint() { return deviceFingerprint; }
        public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
        public long getCreateTime() { return createTime; }
        public void setCreateTime(long createTime) { this.createTime = createTime; }
        public boolean isUsed() { return used; }
        public void setUsed(boolean used) { this.used = used; }
    }
} 