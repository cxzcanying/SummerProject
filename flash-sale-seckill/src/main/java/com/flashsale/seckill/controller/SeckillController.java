package com.flashsale.seckill.controller;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.seckill.dto.FlashSaleActivityDTO;
import com.flashsale.seckill.dto.FlashSaleProductDTO;
import com.flashsale.seckill.service.FlashSaleActivityService;
import com.flashsale.seckill.service.FlashSaleProductService;
import com.flashsale.seckill.service.RateLimitService;
import com.flashsale.seckill.service.SeckillService;
import com.flashsale.seckill.vo.FlashSaleActivityVO;
import com.flashsale.seckill.vo.FlashSaleProductVO;
import com.flashsale.seckill.vo.SeckillOrderVO;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
    
    @Autowired
    private RateLimitService rateLimitService;

    /**
     * 提交秒杀请求
     */
    @PostMapping("/submit")
    @SentinelResource(value = "doSeckill", blockHandler = "handleBlock")
    public Result<String> doSeckill(@RequestBody @Valid SeckillDTO seckillDTO, HttpServletRequest request) {
        log.info("用户{}执行秒杀，商品ID：{}", seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        
        // 限流检查
        String key = "seckill:" + seckillDTO.getUserId() + ":" + seckillDTO.getFlashSaleProductId();
        Result<Boolean> allowResult = rateLimitService.isAllowed(key, 5, 60);
        if (!allowResult.getData()) {
            return Result.error("访问过于频繁，请稍后再试");
        }
        
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
     * 生成秒杀令牌
     */
    @PostMapping("/token/generate")
    @SentinelResource(value = "seckill-token", blockHandler = "handleTokenBlock")
    public Result<String> generateSeckillToken(@RequestBody SeckillTokenRequest tokenRequest) {
        log.info("为用户{}生成秒杀令牌，商品ID：{}", tokenRequest.getUserId(), tokenRequest.getFlashSaleProductId());
        return seckillService.generateSeckillToken(tokenRequest.getUserId(), tokenRequest.getFlashSaleProductId());
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

    @Setter
    @Getter
    public static class SeckillTokenRequest {
        private Long flashSaleProductId;
        private Long userId;
    }

    // Sentinel 阻塞处理方法
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
} 