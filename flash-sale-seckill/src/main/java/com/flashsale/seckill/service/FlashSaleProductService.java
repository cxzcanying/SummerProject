package com.flashsale.seckill.service;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.seckill.dto.FlashSaleProductDTO;
import com.flashsale.seckill.vo.FlashSaleProductVO;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 秒杀商品服务接口
 * @author 21311
 */
public interface FlashSaleProductService {

    /**
     * 添加秒杀商品
     */
    Result<Void> addProduct(FlashSaleProductDTO productDTO);

    /**
     * 批量添加秒杀商品
     */
    Result<Void> batchAddProducts(List<FlashSaleProductDTO> productDTOs);

    /**
     * 更新秒杀商品
     */
    Result<Void> updateProduct(Long id, FlashSaleProductDTO productDTO);

    /**
     * 删除秒杀商品
     */
    Result<Void> deleteProduct(Long id);

    /**
     * 批量删除秒杀商品
     */
    Result<Void> batchDeleteProducts(List<Long> ids);

    /**
     * 根据ID查询商品详情
     */
    Result<FlashSaleProductVO> getProductDetail(Long id);

    /**
     * 根据活动ID查询商品列表
     */
    Result<List<FlashSaleProductVO>> getProductsByActivityId(Long activityId);

    /**
     * 根据商品ID查询秒杀商品列表
     */
    Result<List<FlashSaleProductVO>> getProductsByProductId(Long productId);

    /**
     * 分页查询商品列表
     */
    Result<PageResult<FlashSaleProductVO>> listProducts(Integer page, Integer size, Long activityId, Integer status);

    /**
     * 启用商品
     */
    Result<Void> enableProduct(Long id);

    /**
     * 禁用商品
     */
    Result<Void> disableProduct(Long id);

    /**
     * 批量启用商品
     */
    Result<Void> batchEnableProducts(List<Long> ids);

    /**
     * 批量禁用商品
     */
    Result<Void> batchDisableProducts(List<Long> ids);

    /**
     * 扣减商品库存
     */
    Result<Boolean> decreaseStock(Long id, Integer quantity);

    /**
     * 增加商品库存
     */
    Result<Boolean> increaseStock(Long id, Integer quantity);

    /**
     * 获取正在进行的秒杀商品
     */
    Result<List<FlashSaleProductVO>> getActiveProducts();

    /**
     * 获取即将开始的秒杀商品
     */
    Result<List<FlashSaleProductVO>> getUpcomingProducts();

    /**
     * 获取已结束的秒杀商品
     */
    Result<List<FlashSaleProductVO>> getEndedProducts();

    /**
     * 根据价格范围查询秒杀商品
     */
    Result<List<FlashSaleProductVO>> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 根据时间范围查询秒杀商品
     */
    Result<List<FlashSaleProductVO>> getProductsByTimeRange(Date startTime, Date endTime);

    /**
     * 更新秒杀商品价格
     */
    Result<Void> updateProductPrice(Long id, BigDecimal flashSalePrice);

    /**
     * 更新秒杀商品限购数量
     */
    Result<Void> updateFlashSaleLimit(Long id, Integer flashSaleLimit);

    /**
     * 预热秒杀商品到Redis
     */
    Result<Void> preloadProductsToRedis(Long activityId);

    /**
     * 获取商品库存
     */
    Result<Integer> getProductStock(Long id);

    /**
     * 检查商品是否可以参与秒杀
     */
    Result<Boolean> checkProductSeckillEligibility(Long id);

    /**
     * 获取商品销售统计
     */
    Result<Object> getProductSalesStatistics(Long id);
} 