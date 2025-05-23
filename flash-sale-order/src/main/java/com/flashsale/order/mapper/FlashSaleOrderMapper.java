package com.flashsale.order.mapper;

import com.flashsale.order.entity.FlashSaleOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 秒杀订单Mapper接口
 * @author 21311
 */
@Mapper
public interface FlashSaleOrderMapper {
    
    /**
     * 根据ID查找订单
     */
    FlashSaleOrder findById(Long id);
    
    /**
     * 根据订单号查找订单
     */
    FlashSaleOrder findByOrderNo(String orderNo);
    
    /**
     * 根据用户ID查找订单列表
     */
    List<FlashSaleOrder> findByUserId(Long userId);
    
    /**
     * 插入订单
     */
    int insert(FlashSaleOrder order);
    
    /**
     * 根据ID更新订单
     */
    int updateById(FlashSaleOrder order);
    
    /**
     * 根据ID删除订单
     */
    int deleteById(Long id);
    
    /**
     * 更新订单状态
     */
    int updateStatus(Long id, Integer status);
} 