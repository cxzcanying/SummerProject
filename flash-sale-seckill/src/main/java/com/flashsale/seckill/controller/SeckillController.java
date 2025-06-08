package com.flashsale.seckill.controller;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.seckill.dto.FlashSaleActivityDTO;
import com.flashsale.seckill.dto.FlashSaleProductDTO;
import com.flashsale.seckill.service.FlashSaleActivityService;
import com.flashsale.seckill.service.FlashSaleProductService;
import com.flashsale.seckill.service.SeckillService;
import com.flashsale.seckill.service.impl.SeckillServiceImpl;
import com.flashsale.seckill.vo.FlashSaleActivityVO;
import com.flashsale.seckill.vo.FlashSaleProductVO;
import com.flashsale.seckill.vo.SeckillOrderVO;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 秒杀控制器
 * @author 21311
 */
@Slf4j
@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private FlashSaleActivityService activityService;

    @Autowired
    private FlashSaleProductService productService;

    /**
     * 提交秒杀请求
     */
    @PostMapping("/submit")
    @SentinelResource(value = "doSeckill", blockHandler = "handleBlock")
    public Result<String> doSeckill(@RequestBody @Valid SeckillDTO seckillDTO, HttpServletRequest request) {
        log.info("用户{}执行秒杀，商品ID：{}", seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        
        return seckillService.doSeckill(seckillDTO);
    }

    /**
     * 查询秒杀结果
     */
    @GetMapping("/result/{seckillId}")
    @SentinelResource(value = "seckill-query", blockHandler = "handleQueryBlock")
    public Result<String> getSeckillResult(@PathVariable String seckillId) {
        log.info("查询秒杀结果: {}", seckillId);
        return seckillService.getSeckillResult(seckillId);
    }

    /**
     * 生成秒杀令牌（向后兼容）
     */
    @PostMapping("/token/generate")
    @SentinelResource(value = "seckill-token", blockHandler = "handleTokenBlock")
    public Result<String> generateSeckillToken(@RequestBody SeckillTokenRequest tokenRequest) {
        log.info("为用户{}生成秒杀令牌，商品ID：{}", tokenRequest.getUserId(), tokenRequest.getFlashSaleProductId());
        return seckillService.generateSeckillToken(tokenRequest.getUserId(), tokenRequest.getFlashSaleProductId());
    }
    
    /**
     * 生成增强版秒杀令牌（新接口）
     */
    @PostMapping("/token/enhanced")
    @SentinelResource(value = "enhanced-seckill-token", blockHandler = "handleEnhancedTokenBlock")
    public Result<String> generateEnhancedSeckillToken(@RequestBody EnhancedTokenRequest tokenRequest, 
                                                       HttpServletRequest request) {
        log.info("为用户{}生成增强秒杀令牌，商品ID：{}", tokenRequest.getUserId(), tokenRequest.getFlashSaleProductId());
        
        // 获取客户端IP
        String userIp = getClientIp(request);
        
        // 调用增强令牌生成服务
        return ((SeckillServiceImpl) seckillService).generateEnhancedSeckillToken(
            tokenRequest.getUserId(), 
            tokenRequest.getFlashSaleProductId(),
            userIp,
            tokenRequest.getDeviceFingerprint(),
            tokenRequest.getUserLevel(),
            tokenRequest.getCreditScore(),
            tokenRequest.getIsVerified(),
            tokenRequest.getChallengeAnswer()
        );
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping("/activity/create")
    public Result<Void> createSeckillActivity(@RequestBody @Valid FlashSaleActivityDTO activityDTO) {
        log.info("创建秒杀活动: {}", activityDTO.getName());
        return activityService.createActivity(activityDTO);
    }
    
    /**
     * 查询秒杀活动列表
     */
    @GetMapping("/activity/list")
    public Result<PageResult<FlashSaleActivityVO>> listSeckillActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        log.info("查询秒杀活动列表，页码：{}，大小：{}", page, size);
        return activityService.listActivities(page, size, status);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/activity/detail/{id}")
    public Result<FlashSaleActivityVO> getActivityDetail(@PathVariable Long id) {
        log.info("获取活动详情，活动ID：{}", id);
        return activityService.getActivityDetail(id);
    }

    /**
     * 启动活动
     */
    @PostMapping("/activity/{id}/start")
    public Result<Void> startActivity(@PathVariable Long id) {
        log.info("启动秒杀活动，活动ID：{}", id);
        return activityService.startActivity(id);
    }

    /**
     * 停止活动
     */
    @PostMapping("/activity/{id}/stop")
    public Result<Void> stopActivity(@PathVariable Long id) {
        log.info("停止秒杀活动，活动ID：{}", id);
        return activityService.stopActivity(id);
    }

    /**
     * 添加秒杀商品
     */
    @PostMapping("/product/create")
    public Result<Void> addSeckillProduct(@RequestBody @Valid FlashSaleProductDTO productDTO) {
        log.info("添加秒杀商品，商品ID：{}", productDTO.getProductId());
        return productService.addProduct(productDTO);
    }
    
    /**
     * 获取商品详情
     */
    @GetMapping("/product/detail/{id}")
    public Result<FlashSaleProductVO> getProductDetail(@PathVariable Long id) {
        log.info("获取商品详情，商品ID：{}", id);
        return productService.getProductDetail(id);
    }
    
    /**
     * 获取活动商品列表
     */
    @GetMapping("/product/list")
    public Result<List<FlashSaleProductVO>> getProductsByActivityId(@RequestParam Long activityId) {
        log.info("获取活动商品列表，活动ID：{}", activityId);
        return productService.getProductsByActivityId(activityId);
    }
    
    /**
     * 检查用户是否可以参与秒杀
     */
    @GetMapping("/check/{userId}/{flashSaleProductId}")
    public Result<Boolean> checkSeckillEligibility(
            @PathVariable Long userId, 
            @PathVariable Long flashSaleProductId) {
        log.info("检查用户{}是否可以参与商品{}的秒杀", userId, flashSaleProductId);
        return seckillService.checkSeckillEligibility(userId, flashSaleProductId);
    }
    
    /**
     * 预热秒杀数据
     */
    @PostMapping("/preload/{activityId}")
    public Result<Void> preloadSeckillProducts(@PathVariable Long activityId) {
        log.info("预热秒杀数据，活动ID：{}", activityId);
        return seckillService.preloadSeckillProducts(activityId);
    }
    
    /**
     * 获取秒杀库存
     */
    @GetMapping("/stock/{flashSaleProductId}")
    public Result<Integer> getSeckillStock(@PathVariable Long flashSaleProductId) {
        log.info("获取秒杀库存，商品ID：{}", flashSaleProductId);
        return seckillService.getSeckillStock(flashSaleProductId);
    }
    
    /**
     * 获取用户秒杀订单
     */
    @GetMapping("/order/user/{userId}")
    public Result<PageResult<SeckillOrderVO>> getUserSeckillOrders(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取用户秒杀订单，用户ID：{}", userId);
        return seckillService.getUserSeckillOrders(userId, status, page, size);
    }
    
    /**
     * 支付秒杀订单
     */
    @PostMapping("/order/{orderNo}/pay")
    public Result<String> paySeckillOrder(
            @RequestParam Long userId,
            @PathVariable String orderNo,
            @RequestParam Integer payType) {
        log.info("支付秒杀订单，用户ID：{}，订单号：{}", userId, orderNo);
        return seckillService.paySeckillOrder(userId, orderNo, payType);
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/order/{orderNo}")
    public Result<SeckillOrderVO> getSeckillOrderDetail(@PathVariable String orderNo) {
        log.info("获取订单详情，订单号：{}", orderNo);
        return seckillService.getSeckillOrderDetail(orderNo);
    }
    
    /**
     * 异步秒杀接口 - 削峰填谷
     */
    @PostMapping("/async")
    @SentinelResource(value = "seckill-async", blockHandler = "handleAsyncBlock")
    public Result<String> doSeckillAsync(@RequestBody @Valid SeckillDTO seckillDTO, HttpServletRequest request) {
        log.info("收到异步秒杀请求，用户ID：{}，商品ID：{}", seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        
        // 补充请求信息
        String userIp = getClientIp(request);
        String deviceFingerprint = request.getHeader("Device-Fingerprint");
        String userAgent = request.getHeader("User-Agent");
        
        seckillDTO.setUserIp(userIp);
        seckillDTO.setDeviceFingerprint(deviceFingerprint);
        // userAgent 字段暂时不设置，如需要可以添加到DTO中
        
        return ((SeckillServiceImpl) seckillService).doSeckillAsync(seckillDTO);
    }
    
    /**
     * 查询异步秒杀结果
     */
    @GetMapping("/async/result/{taskId}")
    @SentinelResource(value = "seckill-async-query", blockHandler = "handleAsyncQueryBlock")
    public Result<Object> getSeckillAsyncResult(@PathVariable String taskId) {
        log.info("查询异步秒杀结果，任务ID：{}", taskId);
        return ((SeckillServiceImpl) seckillService).getSeckillAsyncResult(taskId);
    }
    
    /**
     * 检查队列状态
     */
    @GetMapping("/queue/status")
    @SentinelResource(value = "queue-status", blockHandler = "handleQueueStatusBlock")
    public Result<Object> checkQueueStatus(@RequestParam Long userId, @RequestParam Long flashSaleProductId) {
        log.info("检查队列状态，用户ID：{}，商品ID：{}", userId, flashSaleProductId);
        return ((SeckillServiceImpl) seckillService).checkQueueStatus(userId, flashSaleProductId);
    }

    @Setter
    @Getter
    public static class SeckillTokenRequest {
        private Long flashSaleProductId;
        private Long userId;
    }
    
    @Setter
    @Getter
    public static class EnhancedTokenRequest {
        private Long userId;
        private Long flashSaleProductId;
        private String deviceFingerprint;
        private Integer userLevel;
        private Integer creditScore;
        private Boolean isVerified;
        private String challengeAnswer;
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xip = request.getHeader("X-Real-IP");
        String xfor = request.getHeader("X-Forwarded-For");
        
        if (xfor != null && xfor.length() != 0 && !"unknown".equalsIgnoreCase(xfor)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = xfor.indexOf(",");
            if (index != -1) {
                return xfor.substring(0, index);
            } else {
                return xfor;
            }
        }
        
        xfor = xip;
        if (xfor != null && xfor.length() != 0 && !"unknown".equalsIgnoreCase(xfor)) {
            return xfor;
        }
        
        if (xfor == null || xfor.length() == 0 || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("Proxy-Client-IP");
        }
        if (xfor == null || xfor.length() == 0 || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (xfor == null || xfor.length() == 0 || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (xfor == null || xfor.length() == 0 || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (xfor == null || xfor.length() == 0 || "unknown".equalsIgnoreCase(xfor)) {
            xfor = request.getRemoteAddr();
        }
        
        return xfor;
    }

    /**
     * Sentinel 阻塞处理方法
     */
    public Result<String> handleBlock(SeckillDTO seckillDTO, HttpServletRequest request, BlockException ex) {
        log.warn("秒杀请求被限流: {}", ex.getMessage());
        return Result.error(429, "系统繁忙，请稍后重试");
    }

    public Result<String> handleTokenBlock(SeckillTokenRequest tokenRequest, BlockException ex) {
        log.warn("令牌生成请求被限流: {}", ex.getMessage());
        return Result.error(429, "请求过于频繁，请稍后重试");
    }

    public Result<String> handleQueryBlock(String seckillId, BlockException ex) {
        log.warn("查询请求被限流: {}", ex.getMessage());
        return Result.error(429, "查询请求过于频繁，请稍后重试");
    }

    public Result<String> handleEnhancedTokenBlock(EnhancedTokenRequest tokenRequest, HttpServletRequest request, BlockException ex) {
        log.warn("增强令牌生成请求被限流: {}", ex.getMessage());
        return Result.error(429, "令牌生成请求过于频繁，请稍后重试");
    }
    
    /**
     * 异步秒杀限流处理
     */
    public Result<String> handleAsyncBlock(SeckillDTO seckillDTO, HttpServletRequest request, BlockException ex) {
        log.warn("异步秒杀被限流，用户ID：{}，商品ID：{}", seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        return Result.error("系统繁忙，请使用异步处理模式或稍后重试");
    }
    
    /**
     * 异步查询限流处理
     */
    public Result<Object> handleAsyncQueryBlock(String taskId, BlockException ex) {
        log.warn("异步查询被限流，任务ID：{}", taskId);
        return Result.error("查询频次过高，请稍后重试");
    }
    
    /**
     * 队列状态查询限流处理
     */
    public Result<Object> handleQueueStatusBlock(Long userId, Long flashSaleProductId, BlockException ex) {
        log.warn("队列状态查询被限流，用户ID：{}，商品ID：{}", userId, flashSaleProductId);
        return Result.error("查询频次过高，请稍后重试");
    }
} 