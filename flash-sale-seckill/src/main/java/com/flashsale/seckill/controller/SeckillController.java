package com.flashsale.seckill.controller;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.Result;
import com.flashsale.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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

    /**
     * 提交秒杀请求
     */
    @PostMapping("/submit")
    public Result<String> doSeckill(@RequestBody @Valid SeckillDTO seckillDTO) {
        log.info("用户{}执行秒杀，商品ID：{}", seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        return seckillService.doSeckill(seckillDTO);
    }

    /**
     * 查询秒杀结果
     */
    @GetMapping("/result/{seckillId}")
    public Result<String> getSeckillResult(@PathVariable String seckillId) {
        log.info("查询秒杀结果: {}", seckillId);
        // 简化实现，实际应该查询订单状态
        return Result.success("SUCCESS");
    }

    /**
     * 生成秒杀令牌
     */
    @PostMapping("/token/generate")
    public Result<String> generateSeckillToken(@RequestBody SeckillTokenRequest tokenRequest) {
        log.info("为用户{}生成秒杀令牌，活动ID：{}", tokenRequest.getUserId(), tokenRequest.getActivityId());
        return seckillService.generateSeckillToken(tokenRequest.getUserId(), tokenRequest.getActivityId());
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping("/activity/create")
    public Result<Void> createSeckillActivity(@RequestBody SeckillActivityDTO activityDTO) {
        log.info("创建秒杀活动: {}", activityDTO.getActivityName());
        // 简化实现
        return Result.success();
    }

    /**
     * 查询秒杀活动列表
     */
    @GetMapping("/activity/list")
    public Result<Object> listSeckillActivities(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询秒杀活动列表，页码：{}，大小：{}", page, size);
        // 简化实现
        return Result.success(null);
    }

    /**
     * 检查用户秒杀资格
     */
    @GetMapping("/check/{userId}/{flashSaleProductId}")
    public Result<Boolean> checkSeckillEligibility(@PathVariable Long userId, 
                                                   @PathVariable Long flashSaleProductId) {
        log.info("检查用户{}秒杀资格，商品ID：{}", userId, flashSaleProductId);
        return seckillService.checkSeckillEligibility(userId, flashSaleProductId);
    }

    /**
     * 预热秒杀商品
     */
    @PostMapping("/preload/{activityId}")
    public Result<Void> preloadSeckillProducts(@PathVariable Long activityId) {
        log.info("预热活动{}的秒杀商品", activityId);
        return seckillService.preloadSeckillProducts(activityId);
    }

    /**
     * 获取秒杀商品库存
     */
    @GetMapping("/stock/{flashSaleProductId}")
    public Result<Integer> getSeckillStock(@PathVariable Long flashSaleProductId) {
        return seckillService.getSeckillStock(flashSaleProductId);
    }

    // 内部类定义
    public static class SeckillTokenRequest {
        private Long activityId;
        private Long userId;
        
        // getters and setters
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }

    public static class SeckillActivityDTO {
        private String activityName;
        private Long productId;
        private String startTime;
        private String endTime;
        
        // getters and setters
        public String getActivityName() { return activityName; }
        public void setActivityName(String activityName) { this.activityName = activityName; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
    }
} 