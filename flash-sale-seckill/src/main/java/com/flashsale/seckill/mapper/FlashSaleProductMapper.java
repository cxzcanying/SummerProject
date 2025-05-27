package com.flashsale.seckill.mapper;

import com.flashsale.seckill.entity.FlashSaleProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 秒杀商品Mapper接口
 * @author 21311
 */
@Mapper
public interface FlashSaleProductMapper {

    /**
     * 插入秒杀商品
     */
    int insert(FlashSaleProduct product);

    /**
     * 批量插入秒杀商品
     */
    int batchInsert(List<FlashSaleProduct> products);

    /**
     * 根据ID查找秒杀商品
     */
    FlashSaleProduct findById(@Param("id") Long id);

    /**
     * 根据商品ID查找秒杀商品
     */
    List<FlashSaleProduct> findByProductId(@Param("productId") Long productId);

    /**
     * 根据ID更新秒杀商品
     */
    int updateById(FlashSaleProduct product);

    /**
     * 根据ID删除秒杀商品
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除秒杀商品
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据活动ID删除秒杀商品
     */
    int deleteByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据活动ID查找秒杀商品列表
     */
    List<FlashSaleProduct> findByActivityId(@Param("activityId") Long activityId);

    /**
     * 分页查询秒杀商品列表
     */
    List<FlashSaleProduct> findByPage(@Param("offset") Integer offset, 
                                      @Param("size") Integer size,
                                      @Param("activityId") Long activityId,
                                      @Param("status") Integer status);

    /**
     * 统计秒杀商品总数
     */
    Long countProducts(@Param("activityId") Long activityId, @Param("status") Integer status);

    /**
     * 更新秒杀商品状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 减少秒杀商品库存
     */
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加秒杀商品库存
     */
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加已售数量
     */
    int increaseStockUsed(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 查询正在进行的秒杀商品
     */
    List<FlashSaleProduct> findActiveProducts();

    /**
     * 查询即将开始的秒杀商品
     */
    List<FlashSaleProduct> findUpcomingProducts();
    
    /**
     * 查询已结束的秒杀商品
     */
    List<FlashSaleProduct> findEndedProducts();
    
    /**
     * 根据价格范围查询秒杀商品
     */
    List<FlashSaleProduct> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                           @Param("maxPrice") BigDecimal maxPrice);
    
    /**
     * 根据时间范围查询秒杀商品
     */
    List<FlashSaleProduct> findByTimeRange(@Param("startTime") Date startTime, 
                                          @Param("endTime") Date endTime);
    
    /**
     * 更新秒杀商品的价格
     */
    int updatePrice(@Param("id") Long id, @Param("flashSalePrice") BigDecimal flashSalePrice);
    
    /**
     * 更新秒杀商品的限购数量
     */
    int updateFlashSaleLimit(@Param("id") Long id, @Param("flashSaleLimit") Integer flashSaleLimit);
    
    /**
     * 批量更新秒杀商品状态
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
} 