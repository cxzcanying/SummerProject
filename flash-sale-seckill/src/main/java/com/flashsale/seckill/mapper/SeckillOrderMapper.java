package com.flashsale.seckill.mapper;

import com.flashsale.seckill.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 秒杀订单Mapper接口
 * @author 21311
 */
@Mapper
public interface SeckillOrderMapper {

    /**
     * 插入订单
     */
    int insert(SeckillOrder order);

    /**
     * 根据ID查找订单
     */
    SeckillOrder findById(@Param("id") Long id);

    /**
     * 根据订单号查找订单
     */
    SeckillOrder findByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 统计用户购买数量
     */
    Integer countUserBought(@Param("userId") Long userId, @Param("flashSaleProductId") Long flashSaleProductId);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

} 