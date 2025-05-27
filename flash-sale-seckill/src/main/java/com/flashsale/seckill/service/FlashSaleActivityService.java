package com.flashsale.seckill.service;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.seckill.dto.FlashSaleActivityDTO;
import com.flashsale.seckill.vo.FlashSaleActivityVO;

import java.util.Date;
import java.util.List;

/**
 * 秒杀活动服务接口
 * @author 21311
 */
public interface FlashSaleActivityService {

    /**
     * 创建秒杀活动
     */
    Result<Void> createActivity(FlashSaleActivityDTO activityDTO);

    /**
     * 批量创建秒杀活动
     */
    Result<Void> batchCreateActivities(List<FlashSaleActivityDTO> activityDTOs);

    /**
     * 更新秒杀活动
     */
    Result<Void> updateActivity(Long id, FlashSaleActivityDTO activityDTO);

    /**
     * 删除秒杀活动
     */
    Result<Void> deleteActivity(Long id);

    /**
     * 批量删除秒杀活动
     */
    Result<Void> batchDeleteActivities(List<Long> ids);

    /**
     * 根据ID查询活动详情
     */
    Result<FlashSaleActivityVO> getActivityDetail(Long id);

    /**
     * 分页查询活动列表
     */
    Result<PageResult<FlashSaleActivityVO>> listActivities(Integer page, Integer size, Integer status);

    /**
     * 启动活动
     */
    Result<Void> startActivity(Long id);

    /**
     * 停止活动
     */
    Result<Void> stopActivity(Long id);

    /**
     * 获取正在进行的活动列表
     */
    Result<List<FlashSaleActivityVO>> getActiveActivities();

    /**
     * 获取即将开始的活动列表
     */
    Result<List<FlashSaleActivityVO>> getUpcomingActivities();
    
    /**
     * 获取已结束的活动列表
     */
    Result<List<FlashSaleActivityVO>> getEndedActivities();
    
    /**
     * 根据时间范围查询活动
     */
    Result<List<FlashSaleActivityVO>> getActivitiesByTimeRange(Date startTime, Date endTime);
    
    /**
     * 根据名称模糊查询活动
     */
    Result<List<FlashSaleActivityVO>> getActivitiesByName(String name);
    
    /**
     * 更新活动时间
     */
    Result<Void> updateActivityTime(Long id, Date startTime, Date endTime);
    
    /**
     * 获取活动统计信息
     */
    Result<Object> getActivityStatistics(Long id);
    
    /**
     * 检查活动是否可以开始
     */
    Result<Boolean> checkActivityCanStart(Long id);
    
    /**
     * 预热活动数据到缓存
     */
    Result<Void> preloadActivityCache(Long id);
} 