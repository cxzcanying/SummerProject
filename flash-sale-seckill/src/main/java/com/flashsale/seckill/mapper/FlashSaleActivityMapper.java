package com.flashsale.seckill.mapper;

import com.flashsale.seckill.entity.FlashSaleActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 秒杀活动Mapper接口
 * @author 21311
 */
@Mapper
public interface FlashSaleActivityMapper {

    /**
     * 插入秒杀活动
     */
    int insert(FlashSaleActivity activity);

    /**
     * 根据ID查找活动
     */
    FlashSaleActivity findById(@Param("id") Long id);

    /**
     * 根据ID更新活动
     */
    int updateById(FlashSaleActivity activity);

    /**
     * 根据ID删除活动
     */
    int deleteById(@Param("id") Long id);

    /**
     * 分页查询活动列表
     */
    List<FlashSaleActivity> findByPage(@Param("offset") Integer offset, 
                                       @Param("size") Integer size,
                                       @Param("status") Integer status);

    /**
     * 统计活动总数
     */
    Long countActivities(@Param("status") Integer status);

    /**
     * 更新活动状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询正在进行的活动
     */
    List<FlashSaleActivity> findActiveActivities();

    /**
     * 查询即将开始的活动
     */
    List<FlashSaleActivity> findUpcomingActivities();
} 