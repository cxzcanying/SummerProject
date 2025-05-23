package com.flashsale.product.service.impl;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.product.dto.ProductDTO;
import com.flashsale.product.entity.Product;
import com.flashsale.product.mapper.ProductMapper;
import com.flashsale.product.service.ProductService;
import com.flashsale.product.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * @author 21311
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_CACHE_KEY = "product:";
    private static final String PRODUCT_STOCK_KEY = "product:stock:";
    private static final long CACHE_EXPIRE_TIME = 30;
    // 30分钟

    @Override
    @Transactional
    public Result<Void> addProduct(ProductDTO productDTO) {
        try {
            Product product = new Product();
            BeanUtils.copyProperties(productDTO, product);
            product.setCreateTime(new Date());
            product.setUpdateTime(new Date());
            product.setSoldCount(0);
            product.setStatus(1); // 默认上架

            int result = productMapper.insert(product);
            if (result > 0) {
                // 缓存商品信息
                cacheProduct(product);
                // 缓存库存信息
                cacheStock(product.getId(), product.getStock());
                log.info("商品添加成功，商品ID：{}", product.getId());
                return Result.success();
            } else {
                return Result.error("商品添加失败");
            }
        } catch (Exception e) {
            log.error("添加商品异常", e);
            return Result.error("商品添加失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Void> updateProduct(Long id, ProductDTO productDTO) {
        try {
            Product existProduct = productMapper.findById(id);
            if (existProduct == null) {
                return Result.error("商品不存在");
            }

            Product product = new Product();
            BeanUtils.copyProperties(productDTO, product);
            product.setId(id);
            product.setUpdateTime(new Date());

            int result = productMapper.updateById(product);
            if (result > 0) {
                // 删除缓存
                deleteProductCache(id);
                log.info("商品更新成功，商品ID：{}", id);
                return Result.success();
            } else {
                return Result.error("商品更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品异常", e);
            return Result.error("商品更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Void> deleteProduct(Long id) {
        try {
            Product existProduct = productMapper.findById(id);
            if (existProduct == null) {
                return Result.error("商品不存在");
            }

            int result = productMapper.deleteById(id);
            if (result > 0) {
                // 删除缓存
                deleteProductCache(id);
                deleteStockCache(id);
                log.info("商品删除成功，商品ID：{}", id);
                return Result.success();
            } else {
                return Result.error("商品删除失败");
            }
        } catch (Exception e) {
            log.error("删除商品异常", e);
            return Result.error("商品删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result<ProductVO> getProductDetail(Long id) {
        try {
            // 先从缓存获取
            ProductVO productVO = getProductFromCache(id);
            if (productVO != null) {
                return Result.success(productVO);
            }

            // 从数据库获取
            Product product = productMapper.findById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }

            productVO = new ProductVO();
            BeanUtils.copyProperties(product, productVO);

            // 缓存商品信息
            cacheProduct(product);

            return Result.success(productVO);
        } catch (Exception e) {
            log.error("获取商品详情异常", e);
            return Result.error("获取商品详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PageResult<ProductVO>> listProducts(Integer page, Integer size, Long categoryId, String keyword) {
        try {
            // 计算偏移量
            Integer offset = (page - 1) * size;

            // 查询商品列表
            List<Product> products = productMapper.findByPage(offset, size, categoryId, keyword);

            // 查询总数
            Long total = productMapper.countProducts(categoryId, keyword);

            // 转换为VO
            List<ProductVO> productVOList = products.stream()
                    .map(product -> {
                        ProductVO productVO = new ProductVO();
                        BeanUtils.copyProperties(product, productVO);
                        return productVO;
                    })
                    .collect(Collectors.toList());

            PageResult<ProductVO> pageResult = new PageResult<>(productVOList, total, page, size);

            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询商品列表异常", e);
            return Result.error("查询商品列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Void> updateProductStatus(Long id, Integer status) {
        try {
            Product existProduct = productMapper.findById(id);
            if (existProduct == null) {
                return Result.error("商品不存在");
            }

            Product product = new Product();
            product.setId(id);
            product.setStatus(status);
            product.setUpdateTime(new Date());

            int result = productMapper.updateById(product);
            if (result > 0) {
                // 删除缓存
                deleteProductCache(id);
                log.info("商品状态更新成功，商品ID：{}，状态：{}", id, status);
                return Result.success();
            } else {
                return Result.error("商品状态更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品状态异常", e);
            return Result.error("商品状态更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Boolean> decreaseStock(Long productId, Integer quantity) {
        try {
            // 先检查库存
            Integer currentStock = getStockFromCache(productId);
            if (currentStock == null) {
                Product product = productMapper.findById(productId);
                if (product == null) {
                    return Result.error("商品不存在");
                }
                currentStock = product.getStock();
                cacheStock(productId, currentStock);
            }

            if (currentStock < quantity) {
                return Result.success(false);
            }

            // 扣减数据库库存
            int result = productMapper.decreaseStock(productId, quantity);
            if (result > 0) {
                // 更新缓存库存
                updateStockCache(productId, -quantity);
                log.info("库存扣减成功，商品ID：{}，扣减数量：{}", productId, quantity);
                return Result.success(true);
            } else {
                return Result.success(false);
            }
        } catch (Exception e) {
            log.error("扣减库存异常", e);
            return Result.error("扣减库存失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<Boolean> increaseStock(Long productId, Integer quantity) {
        try {
            int result = productMapper.increaseStock(productId, quantity);
            if (result > 0) {
                // 更新缓存库存
                updateStockCache(productId, quantity);
                log.info("库存增加成功，商品ID：{}，增加数量：{}", productId, quantity);
                return Result.success(true);
            } else {
                return Result.success(false);
            }
        } catch (Exception e) {
            log.error("增加库存异常", e);
            return Result.error("增加库存失败：" + e.getMessage());
        }
    }

    /**
     * 缓存商品信息
     */
    private void cacheProduct(Product product) {
        try {
            String key = PRODUCT_CACHE_KEY + product.getId();
            redisTemplate.opsForValue().set(key, product, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("缓存商品信息失败", e);
        }
    }

    /**
     * 从缓存获取商品信息
     */
    private ProductVO getProductFromCache(Long productId) {
        try {
            String key = PRODUCT_CACHE_KEY + productId;
            Product product = (Product) redisTemplate.opsForValue().get(key);
            if (product != null) {
                ProductVO productVO = new ProductVO();
                BeanUtils.copyProperties(product, productVO);
                return productVO;
            }
        } catch (Exception e) {
            log.error("从缓存获取商品信息失败", e);
        }
        return null;
    }

    /**
     * 删除商品缓存
     */
    private void deleteProductCache(Long productId) {
        try {
            String key = PRODUCT_CACHE_KEY + productId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除商品缓存失败", e);
        }
    }

    /**
     * 缓存库存信息
     */
    private void cacheStock(Long productId, Integer stock) {
        try {
            String key = PRODUCT_STOCK_KEY + productId;
            redisTemplate.opsForValue().set(key, stock, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("缓存库存信息失败", e);
        }
    }

    /**
     * 从缓存获取库存信息
     */
    private Integer getStockFromCache(Long productId) {
        try {
            String key = PRODUCT_STOCK_KEY + productId;
            Object stock = redisTemplate.opsForValue().get(key);
            return stock != null ? (Integer) stock : null;
        } catch (Exception e) {
            log.error("从缓存获取库存信息失败", e);
            return null;
        }
    }

    /**
     * 更新缓存库存
     */
    private void updateStockCache(Long productId, Integer delta) {
        try {
            String key = PRODUCT_STOCK_KEY + productId;
            redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("更新缓存库存失败", e);
        }
    }

    /**
     * 删除库存缓存
     */
    private void deleteStockCache(Long productId) {
        try {
            String key = PRODUCT_STOCK_KEY + productId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除库存缓存失败", e);
        }
    }
} 