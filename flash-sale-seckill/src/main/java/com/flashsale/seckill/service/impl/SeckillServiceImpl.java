package com.flashsale.seckill.service.impl;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.PageResult;
import com.flashsale.common.result.Result;
import com.flashsale.common.result.ResultCode;
import com.flashsale.common.constants.UserRole;
import com.flashsale.common.lock.DistributedLockService;
import com.flashsale.common.security.IdempotencyService;
import com.flashsale.common.security.AntiScalpingService;
import com.flashsale.common.security.EnhancedTokenService;
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
import java.util.Map;
import java.util.HashMap;

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

    @Autowired
    private DistributedLockService distributedLockService;

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private AntiScalpingService antiScalpingService;

    @Autowired
    private EnhancedTokenService enhancedTokenService;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private com.flashsale.common.queue.FairRateLimiter fairRateLimiter;

    @Autowired
    private com.flashsale.common.mq.SeckillAsyncProcessor seckillAsyncProcessor;

    private static final String SECKILL_RESULT_KEY = "seckill:result:";
    private static final String SECKILL_TOKEN_KEY = "seckill:token:";
    private static final String SECKILL_LOCK_KEY = "seckill:lock:product:";
    private static final long ORDER_EXPIRE_MINUTES = 30;
    
    // 限流配置
    private static final int MAX_CONCURRENT_PER_PRODUCT = 1000; // 每个商品最大并发数
    private static final int QUEUE_TIMEOUT_SECONDS = 300; // 队列超时时间（5分钟）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> doSeckill(SeckillDTO seckillDTO) {
        
        // 生成请求唯一标识用于幂等性控制
        String requestId = seckillDTO.getUserId() + ":" + seckillDTO.getFlashSaleProductId() + ":" + 
                          (seckillDTO.getRequestId() != null ? seckillDTO.getRequestId() : UUID.randomUUID().toString());
        
        // 1. 公平限流 - 申请进入队列
        String queueToken = fairRateLimiter.requestQueueEntry(
            seckillDTO.getUserId(), 
            seckillDTO.getFlashSaleProductId(), 
            MAX_CONCURRENT_PER_PRODUCT, 
            QUEUE_TIMEOUT_SECONDS
        );
        
        if (queueToken == null) {
            log.warn("用户重复排队或系统繁忙: userId={}, productId={}", 
                    seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
            return Result.error("系统繁忙，请稍后重试");
        }
        
        try {
            // 如果需要排队，返回队列状态
            if (queueToken.startsWith("QUEUED:")) {
                com.flashsale.common.queue.FairRateLimiter.QueueStatus status = 
                    fairRateLimiter.checkQueueStatus(seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
                
                log.info("用户进入排队: userId={}, productId={}, position={}, sequence={}", 
                        seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId(), 
                        status.getPosition(), status.getSequence());
                
                return Result.error("排队中，当前位置: " + status.getPosition() + 
                                  "，请稍后刷新页面或等待系统通知");
            }
            
            // 如果直接进入处理状态，执行完整的秒杀逻辑
            log.info("用户直接进入处理: userId={}, productId={}", 
                    seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
            
            return executeFullSeckillProcess(seckillDTO, requestId);
            
        } catch (Exception e) {
            log.error("执行秒杀异常", e);
            // 完成处理，释放队列资源
            fairRateLimiter.completeProcessing(seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId(), false);
            // 回滚幂等性标识
            idempotencyService.removeIdempotency(requestId);
            return Result.error("秒杀失败：" + e.getMessage());
        }
    }
    
    /**
     * 异步秒杀接口 - 削峰填谷
     */
    public Result<String> doSeckillAsync(SeckillDTO seckillDTO) {
        log.info("收到异步秒杀请求: userId={}, productId={}", 
                seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        
        // 1. 基础验证
        if (seckillDTO.getUserId() == null || seckillDTO.getFlashSaleProductId() == null) {
            return Result.error("参数不能为空");
        }
        
        // 2. 快速防刷验证
        if (!antiScalpingService.checkUserLegitimacy(
                seckillDTO.getUserId(), 
                seckillDTO.getUserIp(), 
                seckillDTO.getDeviceFingerprint(),
                seckillDTO.getUserLevel(), 
                seckillDTO.getCreditScore(), 
                seckillDTO.getIsVerified())) {
            log.warn("用户验证失败: userId={}, ip={}", seckillDTO.getUserId(), seckillDTO.getUserIp());
            return Result.error("用户验证失败，请稍后重试");
        }
        
        // 3. 提交到异步处理队列
        String taskId = seckillAsyncProcessor.submitSeckillRequest(seckillDTO);
        if (taskId == null) {
            return Result.error("系统繁忙，请稍后重试");
        }
        
        log.info("异步秒杀请求已提交: userId={}, productId={}, taskId={}", 
                seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId(), taskId);
        
        return Result.success(taskId);
    }
    
    /**
     * 查询异步秒杀结果
     */
    public Result<Object> getSeckillAsyncResult(String taskId) {
        if (taskId == null || taskId.trim().isEmpty()) {
            return Result.error("任务ID不能为空");
        }
        
        com.flashsale.common.mq.SeckillAsyncProcessor.AsyncTaskResult result = 
            seckillAsyncProcessor.getTaskResult(taskId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("taskId", result.getTaskId());
        response.put("status", result.getStatus());
        response.put("completed", result.isCompleted());
        response.put("success", result.isSuccess());
        
        if (result.isCompleted()) {
            if (result.isSuccess()) {
                response.put("orderNo", result.getResult());
                response.put("message", "秒杀成功");
            } else {
                response.put("message", result.getResult() != null ? result.getResult() : "秒杀失败");
            }
        } else {
            switch (result.getStatus()) {
                case "QUEUED":
                    response.put("message", "排队中，请稍后查询");
                    break;
                case "PROCESSING":
                    response.put("message", "处理中，请稍后查询");
                    break;
                case "NOT_FOUND":
                    response.put("message", "任务不存在或已过期");
                    break;
                default:
                    response.put("message", "未知状态");
            }
        }
        
        return Result.success(response);
    }
    
    /**
     * 检查队列状态
     */
    public Result<Object> checkQueueStatus(Long userId, Long flashSaleProductId) {
        com.flashsale.common.queue.FairRateLimiter.QueueStatus status = 
            fairRateLimiter.checkQueueStatus(userId, flashSaleProductId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", status.getStatus());
        response.put("position", status.getPosition());
        response.put("sequence", status.getSequence());
        response.put("canProceed", status.canProceed());
        
        String message;
        switch (status.getStatus()) {
            case "PROCESSING":
                message = "正在处理您的请求";
                break;
            case "QUEUED":
                message = "排队中，当前位置: " + status.getPosition();
                break;
            case "NOT_FOUND":
                message = "未在队列中";
                break;
            default:
                message = "未知状态";
        }
        response.put("message", message);
        
        return Result.success(response);
    }
    
    /**
     * 执行完整的秒杀流程
     */
    private Result<String> executeFullSeckillProcess(SeckillDTO seckillDTO, String requestId) {
        boolean processingSuccess = false;
        try {
            // 1. 幂等性检查
            if (!idempotencyService.checkSeckillIdempotency(seckillDTO.getUserId(), 
                    seckillDTO.getFlashSaleProductId(), requestId)) {
                log.warn("检测到重复秒杀请求: userId={}, productId={}, requestId={}", 
                        seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId(), requestId);
                return Result.error("请勿重复提交");
            }

            // 2. 验证增强版令牌
            if (seckillDTO.getToken() != null && !enhancedTokenService.validateAndConsumeToken(
                    seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId(), 
                    seckillDTO.getToken(), seckillDTO.getUserIp())) {
                return Result.error("秒杀令牌无效或已过期");
            }

            // 3. 增强版资格检查
            Result<Boolean> checkResult = checkEnhancedSeckillEligibility(
                seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId(),
                seckillDTO.getUserIp(), seckillDTO.getDeviceFingerprint(),
                seckillDTO.getUserRole(), seckillDTO.getUserLevel(),
                seckillDTO.getCreditScore(), seckillDTO.getIsVerified());
            
            if (!checkResult.getCode().equals(ResultCode.SUCCESS.getCode()) || !Boolean.TRUE.equals(checkResult.getData())) {
                return Result.error("不满足秒杀条件: " + checkResult.getMessage());
            }

            // 4. 获取秒杀商品信息
            Result<FlashSaleProductVO> productVoResult = productService.getProductDetail(seckillDTO.getFlashSaleProductId());
            if (!productVoResult.getCode().equals(ResultCode.SUCCESS.getCode()) || productVoResult.getData() == null) {
                return Result.error("获取秒杀商品信息失败");
            }
            FlashSaleProductVO flashSaleProduct = productVoResult.getData();

            // 5. 使用分布式锁保证库存扣减的原子性
            String lockKey = SECKILL_LOCK_KEY + flashSaleProduct.getId();
            Result<String> result = distributedLockService.executeWithLock(lockKey, 1, 10, TimeUnit.SECONDS, () -> {
                
                // 再次检查库存（双重检查锁模式）
                Result<Integer> stockResult = productService.getProductStock(flashSaleProduct.getId());
                if (!stockResult.getCode().equals(ResultCode.SUCCESS.getCode()) || 
                    stockResult.getData() == null || stockResult.getData() < seckillDTO.getQuantity()) {
                    throw new RuntimeException("库存不足，剩余库存: " + 
                        (stockResult.getData() != null ? stockResult.getData() : 0));
                }
                
                // 6. 原子性扣减库存
                Result<Boolean> decreaseResult = productService.decreaseStock(flashSaleProduct.getId(), seckillDTO.getQuantity());
                if (!decreaseResult.getCode().equals(ResultCode.SUCCESS.getCode()) || !Boolean.TRUE.equals(decreaseResult.getData())) {
                    throw new RuntimeException("库存扣减失败: " + decreaseResult.getMessage());
                }

                // 7. 创建订单
                SeckillOrder order = createSeckillOrder(seckillDTO, flashSaleProduct);
                
                // 8. 保存订单到数据库
                try {
                    orderMapper.insert(order);
                } catch (Exception e) {
                    log.error("保存订单失败，开始库存回滚: orderNo={}", order.getOrderNo(), e);
                    // 回滚库存
                    productService.increaseStock(flashSaleProduct.getId(), seckillDTO.getQuantity());
                    throw new RuntimeException("订单创建失败: " + e.getMessage());
                }

                // 9. 缓存秒杀结果
                redisTemplate.opsForValue().set(SECKILL_RESULT_KEY + order.getOrderNo(), "SUCCESS", 24, TimeUnit.HOURS);
                
                // 10. 发送支付消息（异步）
                try {
                    paymentMessageProducer.sendPaymentMessage(order.getOrderNo(), order.getUserId(), 
                        order.getPaymentAmount(), (int) ORDER_EXPIRE_MINUTES);
                } catch (Exception e) {
                    log.warn("发送支付消息失败: orderNo={}", order.getOrderNo(), e);
                    // 支付消息发送失败不影响订单创建
                }

                log.info("秒杀成功 - 用户ID: {}, 商品ID: {}, 订单号: {}, 支付金额: {}", 
                        seckillDTO.getUserId(), flashSaleProduct.getId(), order.getOrderNo(), order.getPaymentAmount());
                
                return Result.success(order.getOrderNo());
            });
            
            processingSuccess = result.getCode().equals(ResultCode.SUCCESS.getCode());
            return result;
            
        } catch (Exception e) {
            log.error("执行完整秒杀流程异常", e);
            return Result.error("秒杀失败：" + e.getMessage());
        } finally {
            // 完成处理，释放队列资源
            fairRateLimiter.completeProcessing(seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId(), processingSuccess);
        }
    }
    
    /**
     * 创建秒杀订单
     */
    private SeckillOrder createSeckillOrder(SeckillDTO seckillDTO, FlashSaleProductVO flashSaleProduct) {
        SeckillOrder order = new SeckillOrder();
        
        // 生成订单号
        String orderNo = "FS" + System.currentTimeMillis() + 
                        UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        order.setOrderNo(orderNo);
        order.setUserId(seckillDTO.getUserId());
        order.setActivityId(flashSaleProduct.getActivityId());
        order.setProductId(flashSaleProduct.getProductId());
        order.setFlashSaleProductId(flashSaleProduct.getId());
        
        // 商品信息
        order.setProductName(flashSaleProduct.getProductName());
        order.setProductImage(flashSaleProduct.getProductImage());
        order.setFlashSalePrice(flashSaleProduct.getFlashSalePrice());
        order.setQuantity(seckillDTO.getQuantity());

        // 计算支付金额（需要考虑用户等级折扣）
        BigDecimal baseAmount = flashSaleProduct.getFlashSalePrice().multiply(BigDecimal.valueOf(seckillDTO.getQuantity()));
        BigDecimal paymentAmount = calculatePaymentAmount(baseAmount, seckillDTO.getUserLevel(), seckillDTO.getUserRole());
        order.setPaymentAmount(paymentAmount);
        
        // 订单状态和时间
        order.setStatus(0); // 待支付状态
        Date now = new Date();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        
        // 计算过期时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, (int) ORDER_EXPIRE_MINUTES);
        order.setExpireTime(calendar.getTime());
        
        return order;
    }
    
    /**
     * 计算支付金额（考虑用户等级和角色折扣）
     */
    private BigDecimal calculatePaymentAmount(BigDecimal baseAmount, Integer userLevel, Integer userRole) {
        if (baseAmount == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discountRate = BigDecimal.ONE; // 默认无折扣
        
        // VIP用户折扣
        if (UserRole.isVip(userRole)) {
            discountRate = new BigDecimal("0.95"); // VIP 95折
        }
        
        // 高等级用户额外折扣
        if (userLevel != null && userLevel >= 5) {
            discountRate = discountRate.multiply(new BigDecimal("0.98")); // 高等级用户再98折
        }
        
        return baseAmount.multiply(discountRate).setScale(2, BigDecimal.ROUND_HALF_UP);
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

            // 2. 生成简单令牌（向后兼容）
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
    
    /**
     * 生成增强版秒杀令牌（新接口）
     */
    public Result<String> generateEnhancedSeckillToken(Long userId, Long flashSaleProductId, 
                                                      String userIp, String deviceFingerprint,
                                                      Integer userLevel, Integer creditScore, 
                                                      Boolean isVerified, String challengeAnswer) {
        try {
            // 1. 检查用户角色权限
            if (!UserRole.canSeckill(userLevel)) {
                return Result.error("用户角色无秒杀权限");
            }
            
            // 2. 检查秒杀资格
            Result<Boolean> checkResult = checkSeckillEligibility(userId, flashSaleProductId);
            if (!checkResult.getCode().equals(ResultCode.SUCCESS.getCode()) || !Boolean.TRUE.equals(checkResult.getData())) {
                return Result.error("不满足秒杀条件: " + checkResult.getMessage());
            }

            // 3. 使用增强令牌服务生成令牌
            String enhancedToken = enhancedTokenService.generateEnhancedToken(
                userId, flashSaleProductId, userIp, deviceFingerprint,
                userLevel, creditScore, isVerified, challengeAnswer);
                
            if (enhancedToken == null) {
                return Result.error("令牌生成失败，请稍后重试或完成身份验证");
            }

            log.info("成功生成增强令牌: userId={}, productId={}", userId, flashSaleProductId);
            return Result.success(enhancedToken);
        } catch (Exception e) {
            log.error("生成增强秒杀令牌异常", e);
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
            
            // 2. 检查是否已购买
            Integer boughtCount = orderMapper.countUserBought(userId, flashSaleProductId);
            log.info("检查秒杀资格 - 用户已购买数量: {}, 限购数量: {}", boughtCount, product.getFlashSaleLimit());
            
            if (product.getFlashSaleLimit() != null && boughtCount != null && boughtCount >= product.getFlashSaleLimit()) {
                 log.warn("检查秒杀资格失败 - 超出限购数量, 用户ID: {}, 商品ID: {}, 已购买: {}, 限购: {}", 
                         userId, flashSaleProductId, boughtCount, product.getFlashSaleLimit());
                 return Result.error(ResultCode.SECKILL_REPEATED.getMessage());
            }

            // 3. 检查库存
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
    
    /**
     * 增强版秒杀资格检查（包含防黄牛检查）
     */
    public Result<Boolean> checkEnhancedSeckillEligibility(Long userId, Long flashSaleProductId,
                                                          String userIp, String deviceFingerprint,
                                                          Integer userRole, Integer userLevel,
                                                          Integer creditScore, Boolean isVerified) {
        try {
            log.info("检查增强秒杀资格 - 用户ID: {}, 商品ID: {}, IP: {}", userId, flashSaleProductId, userIp);
            
            // 1. 检查用户角色权限
            if (!UserRole.canSeckill(userRole)) {
                log.warn("用户角色无秒杀权限: userId={}, role={}", userId, userRole);
                return Result.error("用户角色无秒杀权限");
            }
            
            // 2. 防黄牛检查
            if (!antiScalpingService.checkUserLegitimacy(userId, userIp, deviceFingerprint, 
                    userLevel, creditScore, isVerified)) {
                log.warn("用户未通过防黄牛检查: userId={}, ip={}", userId, userIp);
                return Result.error("账号存在风险，请稍后重试或联系客服");
            }
            
            // 3. 调用基础资格检查
            return checkSeckillEligibility(userId, flashSaleProductId);
            
        } catch (Exception e) {
            log.error("检查增强秒杀资格异常 - 用户ID: {}, 商品ID: {}", userId, flashSaleProductId, e);
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