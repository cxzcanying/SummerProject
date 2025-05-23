package com.flashsale.order.service;

import com.flashsale.common.result.Result;
import com.flashsale.order.entity.FlashSaleOrder;
import com.flashsale.seckill.dto.SeckillDTO;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建秒杀订单
     *
     * @param seckillDTO 秒杀请求
     * @return 订单结果
     */
    Result<FlashSaleOrder> createSeckillOrder(SeckillDTO seckillDTO);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    Result<FlashSaleOrder> getOrderByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    Result<List<FlashSaleOrder>> getOrdersByUserId(Long userId);

    /**
     * 支付订单
     *
     * @param orderNo  订单号
     * @param payType  支付方式
     * @return 支付结果
     */
    Result<Void> payOrder(String orderNo, Integer payType);

    /**
     * 取消订单
     *
     * @param orderNo 订单号
     * @return 取消结果
     */
    Result<Void> cancelOrder(String orderNo);

    /**
     * 完成订单
     *
     * @param orderNo 订单号
     * @return 完成结果
     */
    Result<Void> completeOrder(String orderNo);

    /**
     * 订单超时处理
     *
     * @param orderNo 订单号
     * @return 处理结果
     */
    Result<Void> handleExpiredOrder(String orderNo);
} 