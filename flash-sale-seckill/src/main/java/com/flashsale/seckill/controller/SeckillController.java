package com.flashsale.seckill.controller;

import com.flashsale.common.result.Result;
import com.flashsale.seckill.dto.SeckillDTO;
import com.flashsale.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 秒杀控制器
 * @author 21311
 */
@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 执行秒杀
     */
    @PostMapping("/execute")
    public Result<String> doSeckill(@RequestBody @Valid SeckillDTO seckillDTO) {
        log.info("用户{}执行秒杀，商品ID：{}", seckillDTO.getUserId(), seckillDTO.getFlashSaleProductId());
        return seckillService.doSeckill(seckillDTO);
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

    /**
     * 生成秒杀令牌
     */
    @PostMapping("/token/{userId}/{flashSaleProductId}")
    public Result<String> generateSeckillToken(@PathVariable Long userId, 
                                               @PathVariable Long flashSaleProductId) {
        log.info("为用户{}生成秒杀令牌，商品ID：{}", userId, flashSaleProductId);
        return seckillService.generateSeckillToken(userId, flashSaleProductId);
    }
} 