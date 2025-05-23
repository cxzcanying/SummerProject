package com.flashsale.product.service;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.product.dto.ProductDTO;
import com.flashsale.product.entity.Product;
import com.flashsale.product.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 添加商品
     *
     * @param productDTO 商品信息
     * @return 添加结果
     */
    Result<Void> addProduct(ProductDTO productDTO);

    /**
     * 更新商品
     *
     * @param id         商品ID
     * @param productDTO 商品信息
     * @return 更新结果
     */
    Result<Void> updateProduct(Long id, ProductDTO productDTO);

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 删除结果
     */
    Result<Void> deleteProduct(Long id);

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    Result<ProductVO> getProductDetail(Long id);

    /**
     * 分页查询商品列表
     *
     * @param page       页码
     * @param size       每页大小
     * @param categoryId 分类ID
     * @param keyword    关键词
     * @return 商品列表
     */
    Result<PageResult<ProductVO>> listProducts(Integer page, Integer size, Long categoryId, String keyword);

    /**
     * 更新商品状态
     *
     * @param id     商品ID
     * @param status 状态：0-下架，1-上架
     * @return 更新结果
     */
    Result<Void> updateProductStatus(Long id, Integer status);

    /**
     * 扣减库存
     *
     * @param productId 商品ID
     * @param quantity  扣减数量
     * @return 扣减结果
     */
    Result<Boolean> decreaseStock(Long productId, Integer quantity);

    /**
     * 增加库存
     *
     * @param productId 商品ID
     * @param quantity  增加数量
     * @return 增加结果
     */
    Result<Boolean> increaseStock(Long productId, Integer quantity);
} 