package com.flashsale.seckill.service.impl;

import com.flashsale.common.dto.SeckillDTO;

import com.flashsale.common.result.PageResult;
import com.flashsale.common.result.Result;
import com.flashsale.common.result.ResultCode;
import com.flashsale.seckill.entity.SeckillOrder;
import com.flashsale.seckill.mapper.SeckillOrderMapper;
import com.flashsale.seckill.mq.PaymentMessageProducer;
import com.flashsale.seckill.service.FlashSaleProductService;
import com.flashsale.seckill.service.SeckillService;
import com.flashsale.seckill.vo.FlashSaleProductVO;
import com.flashsale.seckill.vo.SeckillOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.ArrayList;

/**
 * 秒杀服务实现类
 * @author 21311
 */
@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SeckillOrderMapper orderMapper;

    @Autowired
    private FlashSaleProductService productService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PaymentMessageProducer paymentMessageProducer;

    private static final String SECKILL_RESULT_KEY = "seckill:result:";
    private static final String SECKILL_TOKEN_KEY = "seckill:token:";
    private static final long ORDER_EXPIRE_MINUTES = 30;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> doSeckill(SeckillDTO seckillDTO) {
        try {
            // 0. Verify token
            String tokenKey = SECKILL_TOKEN_KEY + seckillDTO.getUserId() + ":" + seckillDTO.getFlashSaleProductId();
            String cachedToken = (String) redisTemplate.opsForValue().get(tokenKey);
            if (seckillDTO.getToken() == null || !seckillDTO.getToken().equals(cachedToken)) {
                return Result.error("秒杀令牌无效或已过期");
            }

            // 1. 检查秒杀资格
            Result<Boolean> checkResult = checkSeckillEligibility(seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
            if (!checkResult.getCode().equals(ResultCode.SUCCESS.getCode()) || !Boolean.TRUE.equals(checkResult.getData())) {
                return Result.error("不满足秒杀条件: " + checkResult.getMessage());
            }

            // 2. 获取秒杀商品信息，用于获取价格
            Result<FlashSaleProductVO> productVoResult = productService.getProductDetail(seckillDTO.getFlashSaleProductId());
            if (!productVoResult.getCode().equals(ResultCode.SUCCESS.getCode()) || productVoResult.getData() == null) {
                return Result.error("获取秒杀商品信息失败");
            }
            FlashSaleProductVO flashSaleProduct = productVoResult.getData();

            // 3. 扣减库存
            Result<Boolean> stockResult = productService.decreaseStock(flashSaleProduct.getId(), seckillDTO.getQuantity());
            if (!stockResult.getCode().equals(ResultCode.SUCCESS.getCode()) || !Boolean.TRUE.equals(stockResult.getData())) {
                return Result.error("库存不足");
            }

            // 4. 创建订单
            SeckillOrder order = new SeckillOrder();
            String orderNo = "FS" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            order.setOrderNo(orderNo);
            order.setUserId(seckillDTO.getUserId());
            order.setActivityId(flashSaleProduct.getActivityId());
            order.setProductId(flashSaleProduct.getProductId());
            order.setFlashSaleProductId(flashSaleProduct.getId());
            
            order.setProductName(flashSaleProduct.getProductName());
            order.setProductImage(flashSaleProduct.getProductImage());
            
            // 保存原价到兼容字段
            order.setOriginalPrice(flashSaleProduct.getOriginalPrice());

            order.setFlashSalePrice(flashSaleProduct.getFlashSalePrice());
            order.setQuantity(seckillDTO.getQuantity());

            // 计算支付金额
            BigDecimal paymentAmount = flashSaleProduct.getFlashSalePrice().multiply(BigDecimal.valueOf(seckillDTO.getQuantity()));
            // 设置支付金额
            order.setPaymentAmount(paymentAmount);
            
            // 兼容字段设置
            order.setPayAmount(paymentAmount);
            order.setDiscountAmount(BigDecimal.ZERO);

            // 设置状态为待支付
            order.setStatus(0);

            // 设置过期时间到兼容字段
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, (int)ORDER_EXPIRE_MINUTES);
            order.setExpireTime(calendar.getTime());

            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());
            orderMapper.insert(order);

            // 5. 缓存秒杀结果
            redisTemplate.opsForValue().set(SECKILL_RESULT_KEY + orderNo, "SUCCESS", 24, TimeUnit.HOURS);
            // 使用订单号作为秒杀ID

            return Result.success(orderNo);
        } catch (Exception e) {
            log.error("执行秒杀异常", e);
            return Result.error("秒杀失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> getSeckillResult(String seckillId) {
        try {
            // 1. 查询Redis中的秒杀结果
            String result = (String) redisTemplate.opsForValue().get(SECKILL_RESULT_KEY + seckillId);
            if (result != null) {
                return Result.success(result);
            }

            // 2. 直接查询订单状态
            SeckillOrder order = orderMapper.findByOrderNo(seckillId);
            if (order == null) {
                return Result.error("订单不存在");
            }

            // 3. 返回订单状态
            String status = switch (order.getStatus()) {
                case 1 -> "SUCCESS";
                case 2 -> "FAILED";
                case 3 -> "CANCELLED";
                default -> "UNKNOWN";
            };

            // 4. 缓存结果
            redisTemplate.opsForValue().set(SECKILL_RESULT_KEY + seckillId, status, 24, TimeUnit.HOURS);

            return Result.success(status);
        } catch (Exception e) {
            log.error("查询秒杀结果异常", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> generateSeckillToken(Long userId, Long flashSaleProductId) {
        try {
            // 1. 检查秒杀资格
            Result<Boolean> checkResult = checkSeckillEligibility(userId, flashSaleProductId);
            if (!checkResult.getCode().equals(ResultCode.SUCCESS.getCode()) || !Boolean.TRUE.equals(checkResult.getData())) {
                return Result.error("不满足秒杀条件: " + checkResult.getMessage());
            }

            // 2. 生成令牌
            String token = userId + "_" + flashSaleProductId + "_" + System.currentTimeMillis();
            
            // 3. 缓存令牌
            String tokenKey = SECKILL_TOKEN_KEY + userId + ":" + flashSaleProductId;
            redisTemplate.opsForValue().set(tokenKey, token, 1, TimeUnit.HOURS);

            return Result.success(token);
        } catch (Exception e) {
            log.error("生成秒杀令牌异常", e);
            return Result.error("生成令牌失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> checkSeckillEligibility(Long userId, Long flashSaleProductId) {
        try {
            log.info("检查秒杀资格 - 用户ID: {}, 商品ID: {}", userId, flashSaleProductId);
            
            // 1. 检查商品是否存在且可秒杀
            Result<FlashSaleProductVO> productResult = productService.getProductDetail(flashSaleProductId);
            if (!productResult.getCode().equals(ResultCode.SUCCESS.getCode()) || productResult.getData() == null) {
                log.warn("检查秒杀资格失败 - 商品不存在, 商品ID: {}", flashSaleProductId);
                return Result.error("商品不存在");
            }

            FlashSaleProductVO product = productResult.getData();
            log.info("检查秒杀资格 - 商品信息: ID={}, 状态={}, 剩余库存={}, 限购数量={}, 可秒杀={}", 
                    product.getId(), product.getStatus(), product.getRemainingStock(), 
                    product.getFlashSaleLimit(), product.getCanSeckill());
                    
            if (!Boolean.TRUE.equals(product.getCanSeckill())) {
                log.warn("检查秒杀资格失败 - 商品不可秒杀, 商品ID: {}", flashSaleProductId);
                return Result.error("商品不可秒杀");
            }
            
            // 3. 检查是否已购买
            Integer boughtCount = orderMapper.countUserBought(userId, flashSaleProductId);
            log.info("检查秒杀资格 - 用户已购买数量: {}, 限购数量: {}", boughtCount, product.getFlashSaleLimit());
            
            if (product.getFlashSaleLimit() != null && boughtCount != null && boughtCount >= product.getFlashSaleLimit()) {
                 log.warn("检查秒杀资格失败 - 超出限购数量, 用户ID: {}, 商品ID: {}, 已购买: {}, 限购: {}", 
                         userId, flashSaleProductId, boughtCount, product.getFlashSaleLimit());
                 return Result.error(ResultCode.SECKILL_REPEATED.getMessage());
            }

            // 4. 检查库存
            if (product.getRemainingStock() == null || product.getRemainingStock() <= 0) {
                log.warn("检查秒杀资格失败 - 库存不足, 商品ID: {}, 剩余库存: {}", 
                        flashSaleProductId, product.getRemainingStock());
                return Result.error(ResultCode.PRODUCT_STOCK_NOT_ENOUGH.getMessage());
            }

            log.info("检查秒杀资格成功 - 用户ID: {}, 商品ID: {}", userId, flashSaleProductId);
            return Result.success(true);
        } catch (Exception e) {
            log.error("检查秒杀资格异常 - 用户ID: {}, 商品ID: {}", userId, flashSaleProductId, e);
            return Result.error("检查失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> preloadSeckillProducts(Long activityId) {
        try {
            return productService.preloadProductsToRedis(activityId);
        } catch (Exception e) {
            log.error("预热秒杀商品异常", e);
            return Result.error("预热失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Integer> getSeckillStock(Long flashSaleProductId) {
        try {
            Result<FlashSaleProductVO> productResult = productService.getProductDetail(flashSaleProductId);
            if (!productResult.getCode().equals(ResultCode.SUCCESS.getCode()) || productResult.getData() == null) {
                return Result.error("商品不存在");
            }

            return Result.success(productResult.getData().getRemainingStock());
        } catch (Exception e) {
            log.error("获取秒杀库存异常", e);
            return Result.error("获取库存失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PageResult<SeckillOrderVO>> getUserSeckillOrders(Long userId, Integer status, Integer page, Integer size) {
        try {
            // TODO 简化实现，直接返回空列表
            return Result.success(new PageResult<>(new ArrayList<>(), 0L, page, size));
        } catch (Exception e) {
            log.error("获取用户秒杀订单异常", e);
            return Result.error("获取订单失败：" + e.getMessage());
        }
    }

    @Override
    public Result<String> paySeckillOrder(Long userId, String orderNo, Integer payType) {
        try {
            log.info("开始异步支付秒杀订单 - 用户ID: {}, 订单号: {}, 支付方式: {}", userId, orderNo, payType);
            
            // 1. 查询订单
            SeckillOrder order = orderMapper.findByOrderNo(orderNo);
            if (order == null) {
                return Result.error("订单不存在");
            }
            
            // 2. 验证订单所有者
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权操作此订单");
            }
            
            // 3. 验证订单状态
            if (order.getStatus() != 0) {
                return Result.error("订单状态不支持支付");
            }
            
            // 4. 验证订单是否过期
            if (order.getExpireTime() != null && order.getExpireTime().before(new Date())) {
                // 更新订单状态为已超时
                orderMapper.updateStatus(order.getId(), 5);
                return Result.error("订单已过期");
            }
            
            // 5. 发送异步支付消息
            paymentMessageProducer.sendPaymentMessage(
                orderNo, 
                userId, 
                order.getPaymentAmount(), 
                payType
            );
            
            log.info("异步支付消息发送成功 - 订单号: {}", orderNo);
            return Result.success("支付请求已提交，正在处理中...");
            
        } catch (Exception e) {
            log.error("支付秒杀订单异常 - 用户ID: {}, 订单号: {}", userId, orderNo, e);
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    @Override
    public Result<SeckillOrderVO> getSeckillOrderDetail(String orderNo) {
        try {
            SeckillOrder seckillOrder = seckillOrderMapper.findByOrderNo(orderNo);
            if (seckillOrder == null) {
                return Result.error("订单不存在");
            }

            SeckillOrderVO seckillOrderVO = convertToVO(seckillOrder);
            return Result.success(seckillOrderVO);
        } catch (Exception e) {
            log.error("获取秒杀订单详情异常", e);
            return Result.error("获取订单详情失败：" + e.getMessage());
        }
    }

    /**
     * 转换为VO
     */
    private SeckillOrderVO convertToVO(SeckillOrder seckillOrder) {
        SeckillOrderVO seckillOrderVO = new SeckillOrderVO();
        BeanUtils.copyProperties(seckillOrder, seckillOrderVO);

        return seckillOrderVO;
    }
} 