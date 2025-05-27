package com.flashsale.seckill.service.impl;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.seckill.dto.FlashSaleActivityDTO;
import com.flashsale.seckill.entity.FlashSaleActivity;
import com.flashsale.seckill.mapper.FlashSaleActivityMapper;
import com.flashsale.seckill.service.FlashSaleActivityService;
import com.flashsale.seckill.vo.FlashSaleActivityVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 秒杀活动服务实现类
 * @author 21311
 */
@Slf4j
@Service
public class FlashSaleActivityServiceImpl implements FlashSaleActivityService {

    @Autowired
    private FlashSaleActivityMapper activityMapper;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createActivity(FlashSaleActivityDTO activityDTO) {
        try {
            FlashSaleActivity activity = new FlashSaleActivity();
            BeanUtils.copyProperties(activityDTO, activity);
            activity.setStatus(0);
            // 未开始
            activity.setCreateTime(new Date());
            activity.setUpdateTime(new Date());

            int result = activityMapper.insert(activity);
            if (result > 0) {
                log.info("创建秒杀活动成功，活动ID：{}", activity.getId());
                return Result.success();
            } else {
                return Result.error("创建活动失败");
            }
        } catch (Exception e) {
            log.error("创建秒杀活动异常", e);
            return Result.error("创建活动失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchCreateActivities(List<FlashSaleActivityDTO> activityDTOs) {
        try {
            List<FlashSaleActivity> activities = new ArrayList<>();
            Date now = new Date();
            
            for (FlashSaleActivityDTO dto : activityDTOs) {
                FlashSaleActivity activity = new FlashSaleActivity();
                BeanUtils.copyProperties(dto, activity);
                activity.setStatus(0); // 未开始
                activity.setCreateTime(now);
                activity.setUpdateTime(now);
                activities.add(activity);
            }
            
            int result = activityMapper.batchInsert(activities);
            if (result > 0) {
                log.info("批量创建秒杀活动成功，数量：{}", activities.size());
                return Result.success();
            } else {
                return Result.error("批量创建活动失败");
            }
        } catch (Exception e) {
            log.error("批量创建秒杀活动异常", e);
            return Result.error("批量创建活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateActivity(Long id, FlashSaleActivityDTO activityDTO) {
        try {
            FlashSaleActivity existActivity = activityMapper.findById(id);
            if (existActivity == null) {
                return Result.error("活动不存在");
            }

            FlashSaleActivity activity = new FlashSaleActivity();
            BeanUtils.copyProperties(activityDTO, activity);
            activity.setId(id);
            activity.setUpdateTime(new Date());

            int result = activityMapper.updateById(activity);
            if (result > 0) {
                log.info("更新秒杀活动成功，活动ID：{}", id);
                return Result.success();
            } else {
                return Result.error("更新活动失败");
            }
        } catch (Exception e) {
            log.error("更新秒杀活动异常", e);
            return Result.error("更新活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteActivity(Long id) {
        try {
            FlashSaleActivity existActivity = activityMapper.findById(id);
            if (existActivity == null) {
                return Result.error("活动不存在");
            }

            int result = activityMapper.deleteById(id);
            if (result > 0) {
                log.info("删除秒杀活动成功，活动ID：{}", id);
                return Result.success();
            } else {
                return Result.error("删除活动失败");
            }
        } catch (Exception e) {
            log.error("删除秒杀活动异常", e);
            return Result.error("删除活动失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchDeleteActivities(List<Long> ids) {
        try {
            int result = activityMapper.batchDeleteByIds(ids);
            if (result > 0) {
                log.info("批量删除秒杀活动成功，数量：{}", ids.size());
                return Result.success();
            } else {
                return Result.error("批量删除活动失败");
            }
        } catch (Exception e) {
            log.error("批量删除秒杀活动异常", e);
            return Result.error("批量删除活动失败：" + e.getMessage());
        }
    }

    @Override
    public Result<FlashSaleActivityVO> getActivityDetail(Long id) {
        try {
            FlashSaleActivity activity = activityMapper.findById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }

            FlashSaleActivityVO activityVO = convertToVO(activity);
            return Result.success(activityVO);
        } catch (Exception e) {
            log.error("获取活动详情异常", e);
            return Result.error("获取活动详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PageResult<FlashSaleActivityVO>> listActivities(Integer page, Integer size, Integer status) {
        try {
            Integer offset = (page - 1) * size;

            List<FlashSaleActivity> activities = activityMapper.findByPage(offset, size, status);
            Long total = activityMapper.countActivities(status);

            List<FlashSaleActivityVO> activityVOList = activities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            PageResult<FlashSaleActivityVO> pageResult = new PageResult<>(activityVOList, total, page, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询活动列表异常", e);
            return Result.error("查询活动列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> startActivity(Long id) {
        try {
            int result = activityMapper.updateStatus(id, 1);
            if (result > 0) {
                log.info("启动秒杀活动成功，活动ID：{}", id);
                return Result.success();
            } else {
                return Result.error("启动活动失败");
            }
        } catch (Exception e) {
            log.error("启动秒杀活动异常", e);
            return Result.error("启动活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> stopActivity(Long id) {
        try {
            int result = activityMapper.updateStatus(id, 2);
            if (result > 0) {
                log.info("停止秒杀活动成功，活动ID：{}", id);
                return Result.success();
            } else {
                return Result.error("停止活动失败");
            }
        } catch (Exception e) {
            log.error("停止秒杀活动异常", e);
            return Result.error("停止活动失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<FlashSaleActivityVO>> getActiveActivities() {
        try {
            List<FlashSaleActivity> activities = activityMapper.findActiveActivities();
            List<FlashSaleActivityVO> activityVOList = activities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(activityVOList);
        } catch (Exception e) {
            log.error("获取进行中活动列表异常", e);
            return Result.error("获取活动列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<FlashSaleActivityVO>> getUpcomingActivities() {
        try {
            List<FlashSaleActivity> activities = activityMapper.findUpcomingActivities();
            List<FlashSaleActivityVO> activityVOList = activities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(activityVOList);
        } catch (Exception e) {
            log.error("获取即将开始活动列表异常", e);
            return Result.error("获取活动列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<FlashSaleActivityVO>> getEndedActivities() {
        try {
            List<FlashSaleActivity> activities = activityMapper.findEndedActivities();
            List<FlashSaleActivityVO> activityVOList = activities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(activityVOList);
        } catch (Exception e) {
            log.error("获取已结束活动列表异常", e);
            return Result.error("获取活动列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<FlashSaleActivityVO>> getActivitiesByTimeRange(Date startTime, Date endTime) {
        try {
            List<FlashSaleActivity> activities = activityMapper.findByTimeRange(startTime, endTime);
            List<FlashSaleActivityVO> activityVOList = activities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(activityVOList);
        } catch (Exception e) {
            log.error("根据时间范围获取活动列表异常", e);
            return Result.error("获取活动列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<FlashSaleActivityVO>> getActivitiesByName(String name) {
        try {
            List<FlashSaleActivity> activities = activityMapper.findByNameLike(name);
            List<FlashSaleActivityVO> activityVOList = activities.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(activityVOList);
        } catch (Exception e) {
            log.error("根据名称获取活动列表异常", e);
            return Result.error("获取活动列表失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateActivityTime(Long id, Date startTime, Date endTime) {
        try {
            int result = activityMapper.updateActivityTime(id, startTime, endTime);
            if (result > 0) {
                log.info("更新活动时间成功，活动ID：{}", id);
                return Result.success();
            } else {
                return Result.error("更新活动时间失败");
            }
        } catch (Exception e) {
            log.error("更新活动时间异常", e);
            return Result.error("更新活动时间失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Object> getActivityStatistics(Long id) {
        try {
            // 简化实现，返回基本统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("activityId", id);
            statistics.put("totalProducts", 0);
            statistics.put("totalOrders", 0);
            statistics.put("totalSales", 0);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取活动统计信息异常", e);
            return Result.error("获取活动统计信息失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Boolean> checkActivityCanStart(Long id) {
        try {
            FlashSaleActivity activity = activityMapper.findById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            
            // 检查活动是否可以开始
            Date now = new Date();
            boolean canStart = activity.getStatus() == 0 && 
                              !now.before(activity.getStartTime()) && 
                               now.before(activity.getEndTime());
            
            return Result.success(canStart);
        } catch (Exception e) {
            log.error("检查活动是否可以开始异常", e);
            return Result.error("检查失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Void> preloadActivityCache(Long id) {
        try {
            if (redisTemplate == null) {
                return Result.error("Redis未配置");
            }
            
            FlashSaleActivity activity = activityMapper.findById(id);
            if (activity == null) {
                return Result.error("活动不存在");
            }
            
            // 缓存活动信息
            String key = "activity:" + id;
            redisTemplate.opsForValue().set(key, activity, 24, TimeUnit.HOURS);
            
            log.info("预热活动缓存成功，活动ID：{}", id);
            return Result.success();
        } catch (Exception e) {
            log.error("预热活动缓存异常", e);
            return Result.error("预热活动缓存失败：" + e.getMessage());
        }
    }

    /**
     * 转换为VO
     */
    private FlashSaleActivityVO convertToVO(FlashSaleActivity activity) {
        FlashSaleActivityVO activityVO = new FlashSaleActivityVO();
        BeanUtils.copyProperties(activity, activityVO);
        
        // 设置状态名称
        activityVO.setStatusName(getStatusName(activity.getStatus()));
        
        return activityVO;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "未知";
        };
    }
} 