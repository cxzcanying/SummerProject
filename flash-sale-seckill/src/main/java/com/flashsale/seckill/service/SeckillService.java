package com.flashsale.seckill.service;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.Result;

/**
 * 秒杀服务接口
 * @author 21311
 */
public interface SeckillService {

    /**
     * 执行秒杀
     *
     * @param seckillDTO 秒杀请求参数
     * @return 秒杀结果
     */
    Result<String> doSeckill(SeckillDTO seckillDTO);

    /**
     * 检查用户是否有资格参与秒杀
     *
     * @param userId            用户ID
     * @param flashSaleProductId 秒杀商品ID
     * @return 检查结果
     */
    Result<Boolean> checkSeckillEligibility(Long userId, Long flashSaleProductId);

    /**
     * 预热秒杀商品到Redis
     *
     * @param activityId 活动ID
     * @return 预热结果
     */
    Result<Void> preloadSeckillProducts(Long activityId);

    /**
     * 获取秒杀商品的剩余库存
     *
     * @param flashSaleProductId 秒杀商品ID
     * @return 剩余库存
     */
    Result<Integer> getSeckillStock(Long flashSaleProductId);

    /**
     * 生成秒杀令牌
     *
     * @param userId            用户ID
     * @param flashSaleProductId 秒杀商品ID
     * @return 秒杀令牌
     */
    Result<String> generateSeckillToken(Long userId, Long flashSaleProductId);

    /**
     * 验证秒杀令牌
     *
     * @param userId            用户ID
     * @param flashSaleProductId 秒杀商品ID
     * @param token             令牌
     * @return 验证结果
     */
    Result<Boolean> verifySeckillToken(Long userId, Long flashSaleProductId, String token);
} 