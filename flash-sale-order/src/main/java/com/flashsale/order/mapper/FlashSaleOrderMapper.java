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
     * 根据订单号查找订单
     * @param orderNo 订单编号
     * @return 秒杀订单实体类
     */
    FlashSaleOrder findByOrderNo(String orderNo);
    
    /**
     * 根据用户ID查找订单列表
     */
    List<FlashSaleOrder> findByUserId(Long userId);
    
    /**
     * 根据用户ID查找待付款订单列表
     */
    List<FlashSaleOrder> findPendingOrdersByUserId(Long userId);
    
    /**
     * 插入订单
     */
    int insert(FlashSaleOrder order);
    
    /**
     * 根据ID更新订单
     */
    int updateById(FlashSaleOrder order);
    
    /**
     * 更新订单状态
     */
    int updateStatus(Long id, Integer status);
    
    /**
     * 根据订单ID查找订单
     * @param id 订单ID
     * @return 秒杀订单实体类
     */
    FlashSaleOrder findById(Long id);
} 