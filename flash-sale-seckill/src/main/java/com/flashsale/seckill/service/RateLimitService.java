package com.flashsale.seckill.service;

import com.flashsale.common.result.Result;
import com.flashsale.seckill.entity.RateLimitRecord;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 限流服务接口
 * @author 21311
 */
public interface RateLimitService {

    /**
     * 检查是否允许访问
     *
     * @param key 限流键
     * @param limit 限制次数
     * @param period 时间周期（秒）
     * @return 是否允许访问
     */
    Result<Boolean> isAllowed(String key, Integer limit, Integer period);

    /**
     * 记录访问
     *
     * @param key 限流键
     * @return 记录结果
     */
    Result<Void> recordAccess(String key);

    /**
     * 获取访问次数
     *
     * @param key 限流键
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 访问次数
     */
    Result<Integer> getAccessCount(String key, Date startTime, Date endTime);

    /**
     * 清理过期记录
     *
     * @return 清理结果
     */
    Result<Integer> cleanExpiredRecords();

    /**
     * 获取限流记录
     *
     * @param key 限流键
     * @return 限流记录
     */
    Result<List<RateLimitRecord>> getRateLimitRecords(String key);

    /**
     * 重置限流计数
     *
     * @param key 限流键
     * @return 重置结果
     */
    Result<Void> resetCounter(String key);

    /**
     * 获取热点限流键
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 热点限流键
     */
    Result<List<Map<String, Object>>> getHotKeys(Date startTime, Date endTime, Integer limit);

    /**
     * 设置限流规则
     *
     * @param key 限流键
     * @param limit 限制次数
     * @param period 时间周期（秒）
     * @return 设置结果
     */
    Result<Void> setRateLimit(String key, Integer limit, Integer period);

    /**
     * 获取限流规则
     *
     * @param key 限流键
     * @return 限流规则
     */
    Result<Map<String, Object>> getRateLimit(String key);

    /**
     * 删除限流规则
     *
     * @param key 限流键
     * @return 删除结果
     */
    Result<Void> deleteRateLimit(String key);
} 