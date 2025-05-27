package com.flashsale.seckill.service.impl;

import com.flashsale.common.result.Result;
import com.flashsale.seckill.entity.RateLimitRecord;
import com.flashsale.seckill.mapper.RateLimitRecordMapper;
import com.flashsale.seckill.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 限流服务实现类
 * @author 21311
 */
@Service
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    @Autowired
    private RateLimitRecordMapper rateLimitRecordMapper;

    @Override
    public Result<Boolean> isAllowed(String key, Integer limit, Integer period) {
        try {
            // 获取当前时间戳（秒）
            long now = System.currentTimeMillis() / 1000;
            // 计算时间窗口
            long windowStart = now - (now % period);
            
            // 查询记录
            RateLimitRecord record = rateLimitRecordMapper.findByKeyAndWindow(key, windowStart);
            
            if (record == null) {
                // 创建新记录
                record = new RateLimitRecord();
                record.setKeyName(key);
                record.setCount(1);
                record.setWindowStart(windowStart);
                record.setExpireTime(new Date(windowStart * 1000 + period * 1000));
                record.setCreateTime(new Date());
                rateLimitRecordMapper.insert(record);
                return Result.success(true);
            } else {
                // 检查是否超过限制
                if (record.getCount() >= limit) {
                    return Result.success(false);
                }
                
                // 增加计数
                rateLimitRecordMapper.incrementCount(record.getId(), 1);
                return Result.success(true);
            }
        } catch (Exception e) {
            log.error("限流检查异常", e);
            // 发生异常时默认允许访问，避免限流功能影响正常业务
            return Result.success(true);
        }
    }

    @Override
    public Result<Void> recordAccess(String key) {
        try {
            long now = System.currentTimeMillis() / 1000;
            // 默认使用60秒窗口
            long windowStart = now - (now % 60);
            
            RateLimitRecord record = rateLimitRecordMapper.findByKeyAndWindow(key, windowStart);
            
            if (record == null) {
                record = new RateLimitRecord();
                record.setKeyName(key);
                record.setCount(1);
                record.setWindowStart(windowStart);
                record.setExpireTime(new Date(windowStart * 1000 + 60 * 1000));
                record.setCreateTime(new Date());
                rateLimitRecordMapper.insert(record);
            } else {
                rateLimitRecordMapper.incrementCount(record.getId(), 1);
            }
            
            return Result.success();
        } catch (Exception e) {
            log.error("记录访问异常", e);
            return Result.error("记录访问失败");
        }
    }

    @Override
    public Result<Integer> getAccessCount(String key, Date startTime, Date endTime) {
        try {
            int count = rateLimitRecordMapper.countAccessByKey(key, startTime, endTime);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取访问次数异常", e);
            return Result.error("获取访问次数失败");
        }
    }

    @Override
    public Result<Integer> cleanExpiredRecords() {
        try {
            int count = rateLimitRecordMapper.deleteExpired(new Date());
            return Result.success(count);
        } catch (Exception e) {
            log.error("清理过期记录异常", e);
            return Result.error("清理过期记录失败");
        }
    }

    @Override
    public Result<List<RateLimitRecord>> getRateLimitRecords(String key) {
        try {
            List<RateLimitRecord> records = rateLimitRecordMapper.findByKey(key);
            return Result.success(records);
        } catch (Exception e) {
            log.error("获取限流记录异常", e);
            return Result.error("获取限流记录失败");
        }
    }

    @Override
    public Result<Void> resetCounter(String key) {
        try {
            // 清理该键的所有记录（简单实现）
            List<RateLimitRecord> records = rateLimitRecordMapper.findByKey(key);
            for (RateLimitRecord record : records) {
                rateLimitRecordMapper.deleteExpired(new Date(record.getWindowStart() * 1000));
            }
            return Result.success();
        } catch (Exception e) {
            log.error("重置计数异常", e);
            return Result.error("重置计数失败");
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getHotKeys(Date startTime, Date endTime, Integer limit) {
        // 这里需要自定义实现，因为我们的表结构没有直接提供这种统计
        // 简单实现示例
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            // 实现热点key的统计逻辑
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取热点键异常", e);
            return Result.error("获取热点键失败");
        }
    }

    @Override
    public Result<Void> setRateLimit(String key, Integer limit, Integer period) {
        // 可以实现为将限流规则存储到缓存或配置中
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> getRateLimit(String key) {
        // 可以实现为从缓存或配置中获取限流规则
        Map<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("limit", 100);  // 默认值
        result.put("period", 60);  // 默认值
        return Result.success(result);
    }

    @Override
    public Result<Void> deleteRateLimit(String key) {
        // 可以实现为从缓存或配置中删除限流规则
        return Result.success();
    }
} 