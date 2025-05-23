package com.flashsale.product.mapper;

import com.flashsale.product.entity.FlashSaleProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    int insert(FlashSaleProduct flashSaleProduct);
    
    /**
     * 根据ID删除秒杀商品
     */
    int deleteById(Long id);
    
    /**
     * 根据ID更新秒杀商品
     */
    int updateById(FlashSaleProduct flashSaleProduct);
    
    /**
     * 根据ID查询秒杀商品
     */
    FlashSaleProduct selectById(Long id);

    /**
     * 获取活动中的秒杀商品列表
     *
     * @param activityId 活动ID
     * @return 秒杀商品列表
     */
    @Select("SELECT fsp.* FROM flash_sale_product fsp " +
            "INNER JOIN flash_sale_activity fsa ON fsp.activity_id = fsa.id " +
            "WHERE fsp.activity_id = #{activityId} AND fsp.status = 1 AND fsa.status = 1")
    List<FlashSaleProduct> getFlashSaleProductsByActivityId(@Param("activityId") Long activityId);

    /**
     * 扣减秒杀商品库存
     *
     * @param id       秒杀商品ID
     * @param quantity 扣减数量
     * @return 影响行数
     */
    @Update("UPDATE flash_sale_product SET flash_sale_stock = flash_sale_stock - #{quantity}, " +
            "stock_used = stock_used + #{quantity}, update_time = NOW() " +
            "WHERE id = #{id} AND flash_sale_stock >= #{quantity}")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加秒杀商品库存
     *
     * @param id       秒杀商品ID
     * @param quantity 增加数量
     * @return 影响行数
     */
    @Update("UPDATE flash_sale_product SET flash_sale_stock = flash_sale_stock + #{quantity}, " +
            "stock_used = stock_used - #{quantity}, update_time = NOW() " +
            "WHERE id = #{id} AND stock_used >= #{quantity}")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 根据活动ID和商品ID获取秒杀商品
     *
     * @param activityId 活动ID
     * @param productId  商品ID
     * @return 秒杀商品
     */
    @Select("SELECT * FROM flash_sale_product WHERE activity_id = #{activityId} AND product_id = #{productId} LIMIT 1")
    FlashSaleProduct getFlashSaleProductByActivityIdAndProductId(@Param("activityId") Long activityId, @Param("productId") Long productId);
} 