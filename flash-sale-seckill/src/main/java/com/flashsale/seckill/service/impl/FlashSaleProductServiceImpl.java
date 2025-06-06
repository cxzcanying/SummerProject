package com.flashsale.seckill.service.impl;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.seckill.dto.FlashSaleProductDTO;
import com.flashsale.seckill.entity.FlashSaleActivity;
import com.flashsale.seckill.entity.FlashSaleProduct;
import com.flashsale.seckill.mapper.FlashSaleActivityMapper;
import com.flashsale.seckill.mapper.FlashSaleProductMapper;
import com.flashsale.seckill.service.FlashSaleProductService;
import com.flashsale.seckill.vo.FlashSaleProductVO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

/**
 * 秒杀商品服务实现类
 * @author 21311
 */
@Slf4j
@Service
public class FlashSaleProductServiceImpl implements FlashSaleProductService {

    @Autowired
    private FlashSaleProductMapper productMapper;
    
    @Autowired
    private FlashSaleActivityMapper activityMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RestTemplate restTemplate;

    private static final String SECKILL_PRODUCT_KEY = "seckill:product:";
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> addProduct(FlashSaleProductDTO productDTO) {
        try {
            FlashSaleProduct product = new FlashSaleProduct();
            BeanUtils.copyProperties(productDTO, product);
            
            // 设置限购数量
            product.setFlashSaleLimit(productDTO.getFlashSaleLimit());
            
            // 设置初始值
            product.setStockUsed(0);
            product.setStatus(1);
            // 启用
            product.setCreateTime(new Date());
            product.setUpdateTime(new Date());

            int result = productMapper.insert(product);
            if (result > 0) {
                log.info("添加秒杀商品成功，商品ID：{}", product.getId());
                return Result.success();
            } else {
                return Result.error("添加商品失败");
            }
        } catch (Exception e) {
            log.error("添加秒杀商品异常", e);
            return Result.error("添加商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateProduct(Long id, FlashSaleProductDTO productDTO) {
        try {
            FlashSaleProduct existProduct = productMapper.findById(id);
            if (existProduct == null) {
                return Result.error("商品不存在");
            }

            FlashSaleProduct product = new FlashSaleProduct();
            BeanUtils.copyProperties(productDTO, product);
            
            // 设置限购数量
            product.setFlashSaleLimit(productDTO.getFlashSaleLimit());
            
            // 避免设置startTime和endTime字段
            product.setStartTime(null);
            product.setEndTime(null);
            
            product.setId(id);
            product.setUpdateTime(new Date());

            int result = productMapper.updateById(product);
            if (result > 0) {
                // 清除缓存
                clearProductCache(id);
                log.info("更新秒杀商品成功，商品ID：{}", id);
                return Result.success();
            } else {
                return Result.error("更新商品失败");
            }
        } catch (Exception e) {
            log.error("更新秒杀商品异常", e);
            return Result.error("更新商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteProduct(Long id) {
        try {
            FlashSaleProduct existProduct = productMapper.findById(id);
            if (existProduct == null) {
                return Result.error("商品不存在");
            }

            int result = productMapper.deleteById(id);
            if (result > 0) {
                // 清除缓存
                clearProductCache(id);
                log.info("删除秒杀商品成功，商品ID：{}", id);
                return Result.success();
            } else {
                return Result.error("删除商品失败");
            }
        } catch (Exception e) {
            log.error("删除秒杀商品异常", e);
            return Result.error("删除商品失败：" + e.getMessage());
        }
    }
    


    @Override
    public Result<FlashSaleProductVO> getProductDetail(Long id) {
        try {
            FlashSaleProduct product = productMapper.findById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }

            FlashSaleProductVO productVO = convertToVO(product);
            return Result.success(productVO);
        } catch (Exception e) {
            log.error("获取商品详情异常", e);
            return Result.error("获取商品详情失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<List<FlashSaleProductVO>> getProductsByProductId(Long productId) {
        try {
            List<FlashSaleProduct> products = productMapper.findByProductId(productId);
            List<FlashSaleProductVO> productVOList = products.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(productVOList);
        } catch (Exception e) {
            log.error("根据商品ID获取秒杀商品列表异常", e);
            return Result.error("获取商品列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<FlashSaleProductVO>> getProductsByActivityId(Long activityId) {
        try {
            List<FlashSaleProduct> products = productMapper.findByActivityId(activityId);
            List<FlashSaleProductVO> productVOList = products.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(productVOList);
        } catch (Exception e) {
            log.error("根据活动ID获取商品列表异常", e);
            return Result.error("获取商品列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PageResult<FlashSaleProductVO>> listProducts(Integer page, Integer size, Long activityId, Integer status) {
        try {
            Integer offset = (page - 1) * size;

            List<FlashSaleProduct> products = productMapper.findByPage(offset, size, activityId, status);
            Long total = productMapper.countProducts(activityId, status);

            List<FlashSaleProductVO> productVOList = products.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            PageResult<FlashSaleProductVO> pageResult = new PageResult<>(productVOList, total, page, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询商品列表异常", e);
            return Result.error("查询商品列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> enableProduct(Long id) {
        try {
            int result = productMapper.updateStatus(id, 1);
            if (result > 0) {
                clearProductCache(id);
                log.info("启用秒杀商品成功，商品ID：{}", id);
                return Result.success();
            } else {
                return Result.error("启用商品失败");
            }
        } catch (Exception e) {
            log.error("启用秒杀商品异常", e);
            return Result.error("启用商品失败：" + e.getMessage());
        }
    }
    


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> disableProduct(Long id) {
        try {
            int result = productMapper.updateStatus(id, 0);
            if (result > 0) {
                clearProductCache(id);
                log.info("禁用秒杀商品成功，商品ID：{}", id);
                return Result.success();
            } else {
                return Result.error("禁用商品失败");
            }
        } catch (Exception e) {
            log.error("禁用秒杀商品异常", e);
            return Result.error("禁用商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> decreaseStock(Long id, Integer quantity) {
        try {
            int result = productMapper.decreaseStock(id, quantity);
            if (result > 0) {
                // 更新Redis中的库存
                String stockKey = SECKILL_STOCK_KEY + id;
                redisTemplate.opsForValue().decrement(stockKey, quantity);
                
                // 增加已售数量
                productMapper.increaseStockUsed(id, quantity);
                
                log.info("扣减商品库存成功，商品ID：{}，扣减数量：{}", id, quantity);
                return Result.success(true);
            } else {
                return Result.success(false);
            }
        } catch (Exception e) {
            log.error("扣减商品库存异常", e);
            return Result.error("扣减库存失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> increaseStock(Long id, Integer quantity) {
        try {
            int result = productMapper.increaseStock(id, quantity);
            if (result > 0) {
                // 更新Redis中的库存
                String stockKey = SECKILL_STOCK_KEY + id;
                redisTemplate.opsForValue().increment(stockKey, quantity);
                
                log.info("增加商品库存成功，商品ID：{}，增加数量：{}", id, quantity);
                return Result.success(true);
            } else {
                return Result.success(false);
            }
        } catch (Exception e) {
            log.error("增加商品库存异常", e);
            return Result.error("增加库存失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<FlashSaleProductVO>> getActiveProducts() {
        try {
            List<FlashSaleProduct> products = productMapper.findActiveProducts();
            List<FlashSaleProductVO> productVOList = products.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(productVOList);
        } catch (Exception e) {
            log.error("获取正在进行的秒杀商品异常", e);
            return Result.error("获取商品列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<FlashSaleProductVO>> getUpcomingProducts() {
        try {
            List<FlashSaleProduct> products = productMapper.findUpcomingProducts();
            List<FlashSaleProductVO> productVOList = products.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(productVOList);
        } catch (Exception e) {
            log.error("获取即将开始的秒杀商品异常", e);
            return Result.error("获取商品列表失败：" + e.getMessage());
        }
    }


    @Override
    public Result<Void> preloadProductsToRedis(Long activityId) {
        try {
            List<FlashSaleProduct> products = productMapper.findByActivityId(activityId);
            
            for (FlashSaleProduct product : products) {
                // 缓存商品信息
                String productKey = SECKILL_PRODUCT_KEY + product.getId();
                redisTemplate.opsForValue().set(productKey, product, 24, TimeUnit.HOURS);
                
                // 缓存库存信息
                String stockKey = SECKILL_STOCK_KEY + product.getId();
                redisTemplate.opsForValue().set(stockKey, product.getFlashSaleStock(), 24, TimeUnit.HOURS);
            }
            
            log.info("预热活动{}的秒杀商品到Redis成功，共{}个商品", activityId, products.size());
            return Result.success();
        } catch (Exception e) {
            log.error("预热秒杀商品到Redis异常", e);
            return Result.error("预热失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Integer> getProductStock(Long id) {
        try {
            FlashSaleProduct product = productMapper.findById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            // 计算剩余库存
            int remainingStock = product.getFlashSaleStock() - product.getStockUsed();
            return Result.success(Math.max(remainingStock, 0));
        } catch (Exception e) {
            log.error("获取商品库存异常", e);
            return Result.error("获取库存失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Boolean> checkProductSeckillEligibility(Long id) {
        try {
            FlashSaleProduct product = productMapper.findById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            // 检查商品状态和库存
            boolean canSeckill = product.getStatus() == 1 && product.getFlashSaleStock() > product.getStockUsed();
            
            // 检查活动时间
            if (canSeckill && product.getActivityId() != null) {
                FlashSaleActivity activity = activityMapper.findById(product.getActivityId());
                if (activity != null) {
                    // 检查活动状态
                    canSeckill = canSeckill && activity.getStatus() == 1;
                    
                    Date now = new Date();
                    
                    if (canSeckill && activity.getStartTime() != null) {
                        canSeckill = !now.before(activity.getStartTime());
                    }
                    
                    if (canSeckill && activity.getEndTime() != null) {
                        canSeckill = now.before(activity.getEndTime());
                    }
                    
                    log.info("检查秒杀资格 - 商品ID: {}, 活动ID: {}, 状态: {}, 剩余库存: {}, 活动状态: {}, 可秒杀: {}", 
                            id, activity.getId(), product.getStatus(), 
                            (product.getFlashSaleStock() - product.getStockUsed()), 
                            activity.getStatus(), canSeckill);
                } else {
                    log.warn("检查秒杀资格 - 商品ID: {} 对应的活动ID: {} 不存在", id, product.getActivityId());
                    canSeckill = false;
                }
            } else {
                if (product.getActivityId() == null) {
                    log.warn("检查秒杀资格 - 商品ID: {} 没有关联活动ID", id);
                }
                // 如果没有活动ID，则只根据商品状态和库存判断
                log.info("检查秒杀资格 - 商品ID: {}, 状态: {}, 剩余库存: {}, 可秒杀: {}", 
                        id, product.getStatus(), (product.getFlashSaleStock() - product.getStockUsed()), canSeckill);
            }
            
            return Result.success(canSeckill);
        } catch (Exception e) {
            log.error("检查商品秒杀资格异常, 商品ID: {}", id, e);
            return Result.error("检查失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Object> getProductSalesStatistics(Long id) {
        try {
            FlashSaleProduct product = productMapper.findById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            
            // 简化实现，返回基本统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("productId", id);
            statistics.put("totalStock", product.getFlashSaleStock());
            statistics.put("soldCount", product.getStockUsed());
            statistics.put("remainingStock", product.getFlashSaleStock() - product.getStockUsed());
            
            // 计算销售进度
            if (product.getFlashSaleStock() > 0) {
                int progress = (product.getStockUsed() * 100) / product.getFlashSaleStock();
                statistics.put("progress", Math.min(progress, 100));
            } else {
                statistics.put("progress", 0);
            }
            
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取商品销售统计异常", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }

    /**
     * 转换为VO
     */
    private FlashSaleProductVO convertToVO(FlashSaleProduct product) {
        FlashSaleProductVO productVO = new FlashSaleProductVO();
        BeanUtils.copyProperties(product, productVO);
        
        // 计算剩余库存
        productVO.setRemainingStock(product.getFlashSaleStock() - product.getStockUsed());
        
        // 设置状态名称
        productVO.setStatusName(product.getStatus() == 1 ? "启用" : "禁用");
        
        // 获取商品基础信息
        try {
            if (product.getProductId() != null) {
                // 调用商品服务获取真实的商品信息
                ProductInfo productInfo = getProductInfo(product.getProductId());
                
                if (productInfo != null) {
                    // 使用真实的商品信息
                    productVO.setProductName(productInfo.getName());
                    productVO.setProductImage(productInfo.getMainImage());
                    productVO.setOriginalPrice(productInfo.getPrice());
                    
                    log.debug("成功获取商品信息：{}", productInfo.getName());
                } else {
                    // 降级处理
                    setDefaultProductInfo(productVO, product);
                }
            } else {
                // 没有关联商品ID，使用默认值
                setDefaultProductInfo(productVO, product);
            }
        } catch (Exception e) {
            log.error("获取商品基本信息失败, 商品ID: {}", product.getId(), e);
            // 设置默认值防止NPE
            if (productVO.getProductName() == null) productVO.setProductName("商品" + product.getId());
            if (productVO.getProductImage() == null) productVO.setProductImage("");
            if (productVO.getOriginalPrice() == null) productVO.setOriginalPrice(new BigDecimal("0.00"));
        }
        
        // 从活动中获取开始时间和结束时间
        try {
            if (product.getActivityId() != null) {
                FlashSaleActivity activity = activityMapper.findById(product.getActivityId());
                if (activity != null) {
                    productVO.setStartTime(activity.getStartTime());
                    productVO.setEndTime(activity.getEndTime());
                    
                    // 判断是否可以秒杀
                    Date now = new Date();
                    boolean canSeckill = product.getStatus() == 1 && 
                                        productVO.getRemainingStock() > 0 && 
                                        activity.getStatus() == 1;
                    
                    // 检查活动时间
                    if (canSeckill && activity.getStartTime() != null) {
                        canSeckill = !now.before(activity.getStartTime());
                    }
                    if (canSeckill && activity.getEndTime() != null) {
                        canSeckill = now.before(activity.getEndTime());
                    }
                    
                    log.info("商品ID: {}, 状态: {}, 剩余库存: {}, 活动状态: {}, 当前时间: {}, 开始时间: {}, 结束时间: {}, 可秒杀: {}", 
                            product.getId(), product.getStatus(), productVO.getRemainingStock(), 
                            activity.getStatus(), now, activity.getStartTime(), activity.getEndTime(), canSeckill);
                    
                    productVO.setCanSeckill(canSeckill);
                } else {
                    log.warn("商品ID: {} 对应的活动ID: {} 不存在", product.getId(), product.getActivityId());
                    productVO.setCanSeckill(false);
                }
            } else {
                log.warn("商品ID: {} 没有关联活动ID", product.getId());
                productVO.setCanSeckill(false);
            }
        } catch (Exception e) {
            log.error("获取活动时间信息失败, 商品ID: {}", product.getId(), e);
            productVO.setCanSeckill(false);
        }
        
        if (productVO.getCanSeckill() == null) {
            // 如果上面的尝试失败，使用简化判断
            boolean simpleSeckill = product.getStatus() == 1 && productVO.getRemainingStock() > 0;
            log.info("使用简化判断，商品ID: {}, 可秒杀: {}", product.getId(), simpleSeckill);
            productVO.setCanSeckill(simpleSeckill);
        }
        
        // 计算秒杀进度
        if (product.getFlashSaleStock() > 0) {
            int progress = (product.getStockUsed() * 100) / product.getFlashSaleStock();
            productVO.setProgress(Math.min(progress, 100));
        } else {
            productVO.setProgress(0);
        }
        
        return productVO;
    }
    
    /**
     * 获取商品基础信息
     */
    private ProductInfo getProductInfo(Long productId) {
        try {
            // 调用商品服务获取商品信息
            String url = "http://localhost:8082/api/product/detail/" + productId;
            Map response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.get("code").equals(200)) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");

                ProductInfo productInfo = new ProductInfo();
                productInfo.setName((String) data.get("name"));
                productInfo.setMainImage((String) data.get("mainImage"));
                
                // 处理价格
                Object priceObj = data.get("price");
                if (priceObj instanceof Integer) {
                    productInfo.setPrice(new BigDecimal((Integer) priceObj));
                } else if (priceObj instanceof Double) {
                    productInfo.setPrice(BigDecimal.valueOf((Double) priceObj));
                } else if (priceObj instanceof String) {
                    productInfo.setPrice(new BigDecimal((String) priceObj));
                } else {
                    productInfo.setPrice(new BigDecimal("0.00"));
                }
                
                return productInfo;
            }
            
            return null;
        } catch (Exception e) {
            log.warn("获取商品信息失败，productId：{}", productId, e);
            return null;
        }
    }
    
    /**
     * 设置默认商品信息（降级处理）
     */
    private void setDefaultProductInfo(FlashSaleProductVO productVO, FlashSaleProduct product) {
        // 设置默认商品名称
        if (productVO.getProductName() == null || productVO.getProductName().isEmpty()) {
            productVO.setProductName("商品" + (product.getProductId() != null ? product.getProductId() : product.getId()));
        }
        
        // 设置默认商品图片
        if (productVO.getProductImage() == null || productVO.getProductImage().isEmpty()) {
            productVO.setProductImage("default.jpg");
        }
        
        // 设置默认原价
        if (productVO.getOriginalPrice() == null) {
            if (product.getFlashSalePrice() != null) {
                // 原价设为秒杀价格的1.2倍
                productVO.setOriginalPrice(product.getFlashSalePrice().multiply(new BigDecimal("1.2")));
            } else {
                productVO.setOriginalPrice(new BigDecimal("99.99"));
            }
        }
        
        log.debug("使用默认商品信息，商品ID：{}", product.getId());
    }
    
    /**
     * 商品基础信息内部类
     */
    @Getter
    @Setter
    private static class ProductInfo {
        private String name;
        private String mainImage;
        private BigDecimal price;

    }

    /**
     * 清除产品缓存
     */
    private void clearProductCache(Long productId) {
        try {
            String productKey = SECKILL_PRODUCT_KEY + productId;
            String stockKey = SECKILL_STOCK_KEY + productId;
            redisTemplate.delete(productKey);
            redisTemplate.delete(stockKey);
        } catch (Exception e) {
            log.error("清除商品缓存失败", e);
        }
    }
} 