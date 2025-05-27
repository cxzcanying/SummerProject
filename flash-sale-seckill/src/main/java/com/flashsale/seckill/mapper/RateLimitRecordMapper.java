package com.flashsale.seckill.mapper;

import com.flashsale.seckill.entity.RateLimitRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 限流记录Mapper接口
 * @author 21311
 */
@Mapper
public interface RateLimitRecordMapper {

    /**
     * 插入限流记录
     */
    int insert(RateLimitRecord record);

    /**
     * 根据ID查找限流记录
     */
    RateLimitRecord findById(@Param("id") Long id);

    /**
     * 根据限流键和窗口时间查找记录
     */
    RateLimitRecord findByKeyAndWindow(@Param("keyName") String keyName, 
                                      @Param("windowStart") Long windowStart);

    /**
     * 更新访问次数
     */
    int incrementCount(@Param("id") Long id, @Param("increment") Integer increment);

    /**
     * 更新或插入限流记录
     */
    int upsert(RateLimitRecord record);

    /**
     * 删除过期的限流记录
     */
    int deleteExpired(@Param("currentTime") Date currentTime);

    /**
     * 根据限流键查询记录列表
     */
    List<RateLimitRecord> findByKey(@Param("keyName") String keyName);

    /**
     * 统计指定键在时间范围内的访问次数
     */
    int countAccessByKey(@Param("keyName") String keyName, 
                         @Param("startTime") Date startTime,
                         @Param("endTime") Date endTime);

    /**
     * 批量插入限流记录
     */
    int batchInsert(List<RateLimitRecord> records);

    /**
     * 清空过期时间之前的所有记录
     */
    int clearHistoryRecords(@Param("beforeTime") Date beforeTime);
} 