package com.flashsale.product.mapper;

import com.flashsale.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品Mapper接口
 * @author 21311
 */
@Mapper
public interface ProductMapper {
    
    /**
     * 根据ID查找商品
     */
    Product findById(Long id);
    
    /**
     * 根据分类ID查找商品列表
     */
    List<Product> findByCategoryId(Long categoryId);
    
    /**
     * 查找商品列表
     */
    List<Product> findAll();
    
    /**
     * 插入商品
     */
    int insert(Product product);
    
    /**
     * 根据ID更新商品
     */
    int updateById(Product product);
    
    /**
     * 根据ID删除商品
     */
    int deleteById(Long id);
    
    /**
     * 更新商品库存
     */
    int updateStock(@Param("id") Long id, @Param("stock") Integer stock);
    
    /**
     * 扣减库存
     */
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 增加库存
     */
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
    
    /**
     * 分页查询商品列表
     */
    List<Product> findByPage(@Param("offset") Integer offset, @Param("limit") Integer limit, 
                            @Param("categoryId") Long categoryId, @Param("keyword") String keyword);
    
    /**
     * 统计商品总数
     */
    Long countProducts(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);
} 