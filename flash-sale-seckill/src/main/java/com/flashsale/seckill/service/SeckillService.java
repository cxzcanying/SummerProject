package com.flashsale.seckill.service;

import com.flashsale.common.dto.SeckillDTO;
import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.seckill.vo.SeckillOrderVO;

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
     * 查询秒杀结果
     *
     * @param seckillId 秒杀ID
     * @return 秒杀结果
     */
    Result<String> getSeckillResult(String seckillId);

    /**
     * 检查用户是否有资格参与秒杀
     *
     * @param userId 用户ID
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
     * @param userId 用户ID
     * @param flashSaleProductId 秒杀商品ID
     * @return 秒杀令牌
     */
    Result<String> generateSeckillToken(Long userId, Long flashSaleProductId);

    /**
     * 获取用户秒杀订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Result<PageResult<SeckillOrderVO>> getUserSeckillOrders(Long userId, Integer status, Integer page, Integer size);
    
    /**
     * 支付秒杀订单
     *
     * @param userId 用户ID
     * @param orderNo 订单编号
     * @param payType 支付类型
     * @return 支付结果
     */
    Result<String> paySeckillOrder(Long userId, String orderNo, Integer payType);
    
    /**
     * 查询订单详情
     *
     * @param orderNo 订单编号
     * @return 订单详情
     */
    Result<SeckillOrderVO> getSeckillOrderDetail(String orderNo);
} 