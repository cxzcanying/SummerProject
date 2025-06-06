package com.flashsale.payment.service.impl;

import com.flashsale.common.result.Result;
import com.flashsale.common.result.PageResult;
import com.flashsale.payment.dto.PaymentDTO;
import com.flashsale.payment.entity.Payment;
import com.flashsale.payment.mapper.PaymentMapper;
import com.flashsale.payment.service.PaymentService;
import com.flashsale.payment.vo.PaymentVO;
import com.flashsale.payment.feign.OrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 支付服务实现类
 * @author 21311
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private OrderServiceClient orderServiceClient;

    private static final String PAYMENT_CACHE_KEY = "payment:";
    private static final long CACHE_EXPIRE_TIME = 30;
    // 30分钟

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<PaymentVO> createPayment(PaymentDTO paymentDTO) {
        try {
            log.info("开始创建支付订单，订单ID：{}，用户ID：{}，金额：{}", 
                    paymentDTO.getOrderId(), paymentDTO.getUserId(), paymentDTO.getAmount());
            
            // 检查订单是否已有支付记录（任何状态）
            Payment existPayment = paymentMapper.findByOrderId(paymentDTO.getOrderId());
            if (existPayment != null) {
                if (existPayment.getStatus() == 1) {
                    log.warn("订单已支付，订单ID：{}", paymentDTO.getOrderId());
                    return Result.error("订单已支付，请勿重复支付");
                } else {
                    log.warn("订单已存在待支付记录，订单ID：{}，支付流水号：{}", 
                            paymentDTO.getOrderId(), existPayment.getPaymentNo());
                    // 返回已存在的支付记录
                    PaymentVO paymentVO = convertToVO(existPayment);
                    return Result.success("支付订单已存在", paymentVO);
                }
            }

            // 创建支付记录
            Payment payment = new Payment();
            BeanUtils.copyProperties(paymentDTO, payment);
            
            // 通过Feign调用OrderService获取orderNo
            String realOrderNo = getRealOrderNo(paymentDTO.getOrderId());
            payment.setOrderNo(realOrderNo);
            
            payment.setPaymentNo(generatePaymentNo());
            payment.setStatus(0);
            // 待支付
            payment.setCreateTime(new Date());
            payment.setUpdateTime(new Date());

            int result = paymentMapper.insert(payment);
            if (result > 0) {
                // 缓存支付信息
                cachePayment(payment);

                PaymentVO paymentVO = convertToVO(payment);
                log.info("支付订单创建成功，订单ID：{}，支付流水号：{}", 
                        paymentDTO.getOrderId(), payment.getPaymentNo());
                return Result.success(paymentVO);
            } else {
                log.error("插入支付记录失败，订单ID：{}", paymentDTO.getOrderId());
                return Result.error("创建支付订单失败");
            }
        } catch (Exception e) {
            log.error("创建支付订单异常，订单ID：{}，错误信息：{}", 
                    paymentDTO.getOrderId(), e.getMessage(), e);
            
            // 针对重复键异常的特殊处理
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                return Result.error("订单已存在支付记录，请勿重复创建");
            }
            
            return Result.error("创建支付订单失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> handlePaymentCallback(String paymentNo, String thirdPartyPaymentNo, Integer status) {
        try {
            Payment payment = paymentMapper.findByPaymentNo(paymentNo);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            if (payment.getStatus() == 1) {
                log.warn("支付已成功，无需重复处理，支付流水号：{}", paymentNo);
                return Result.success("支付已完成");
            }

            // 更新支付信息
            int result = paymentMapper.updatePaymentInfo(payment.getId(), thirdPartyPaymentNo, status);
            if (result > 0) {
                // 如果支付成功，更新支付时间
                if (status == 1) {
                    payment.setPaymentTime(new Date());
                    payment.setUpdateTime(new Date());
                    paymentMapper.updateById(payment);
                }

                // 删除缓存
                deletePaymentCache(paymentNo);

                // 根据支付状态返回相应的消息
                String message = getPaymentCallbackMessage(status);
                log.info("支付回调处理成功，支付流水号：{}，状态：{}，消息：{}", paymentNo, status, message);
                return Result.success(message);
            } else {
                return Result.error("支付回调处理失败");
            }
        } catch (Exception e) {
            log.error("处理支付回调异常", e);
            return Result.error("处理支付回调失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PaymentVO> getPaymentDetail(String paymentNo) {
        log.info("开始查询支付详情，支付流水号：{}", paymentNo);
        try {
            // 先从缓存获取
            log.info("尝试从缓存获取支付信息，支付流水号：{}", paymentNo);
            PaymentVO paymentVO = getPaymentFromCache(paymentNo);
            if (paymentVO != null) {
                log.info("从缓存获取支付信息成功，支付流水号：{}", paymentNo);
                return Result.success(paymentVO);
            }

            // 直接尝试不同的查询方式
            log.info("缓存中不存在，尝试从数据库获取支付信息，支付流水号：{}", paymentNo);
            
            // 先尝试通过 subject 字段查询
            log.info("尝试通过 subject 字段查询");
            Payment payment = paymentMapper.findByPaymentNo(paymentNo);
            if (payment != null) {
                log.info("通过 subject 字段查询成功，订单号：{}", payment.getOrderNo());
            } else {
                log.warn("通过 subject 字段查询失败，尝试其他方式");
                
                // 尝试通过订单号查询
                log.info("尝试查询所有订单，查看是否有匹配的支付记录");
                List<Payment> allPayments = paymentMapper.findAllForDebug();
                log.info("数据库中共有 {} 条支付记录", allPayments != null ? allPayments.size() : 0);
                
                if (allPayments != null && !allPayments.isEmpty()) {
                    // 遍历所有记录，查看是否有匹配的支付流水号
                    for (Payment p : allPayments) {
                        log.info("支付记录: ID={}, 订单号={}, 流水号={}, 第三方流水号={}", 
                                p.getId(), p.getOrderNo(), p.getPaymentNo(), p.getThirdPartyPaymentNo());
                        
                        // 检查是否有匹配的记录
                        if (paymentNo.equals(p.getPaymentNo())) {
                            log.info("找到匹配的支付流水号记录");
                            payment = p;
                            break;
                        }
                    }
                }
            }
            
            if (payment == null) {
                log.warn("支付记录不存在，支付流水号：{}", paymentNo);
                return Result.error("支付记录不存在");
            }

            log.info("从数据库获取支付信息成功，支付流水号：{}，订单号：{}", paymentNo, payment.getOrderNo());
            paymentVO = convertToVO(payment);
            // 缓存支付信息
            cachePayment(payment);

            return Result.success(paymentVO);
        } catch (Exception e) {
            log.error("获取支付详情异常，支付流水号：{}", paymentNo, e);
            return Result.error("获取支付详情失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PaymentVO> getPaymentByOrderId(Long orderId) {
        try {
            Payment payment = paymentMapper.findByOrderId(orderId);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            PaymentVO paymentVO = convertToVO(payment);
            return Result.success(paymentVO);
        } catch (Exception e) {
            log.error("根据订单ID获取支付记录异常", e);
            return Result.error("获取支付记录失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PageResult<PaymentVO>> getUserPayments(Long userId, Integer page, Integer size) {
        try {
            // 计算偏移量
            Integer offset = (page - 1) * size;

            // 查询支付记录列表
            List<Payment> payments = paymentMapper.findByUserIdWithPage(userId, offset, size);

            // 查询总数
            Long total = paymentMapper.countByUserId(userId);

            // 转换为VO
            List<PaymentVO> paymentVOList = payments.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            PageResult<PaymentVO> pageResult = new PageResult<>(paymentVOList, total, page, size);

            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询用户支付记录异常", e);
            return Result.error("查询支付记录失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> applyRefund(String paymentNo, BigDecimal refundAmount, String refundReason) {
        try {
            Payment payment = paymentMapper.findByPaymentNo(paymentNo);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            if (payment.getStatus() != 1) {
                return Result.error("只有支付成功的订单才能申请退款");
            }

            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                return Result.error("退款金额不能大于支付金额");
            }

            // 模拟退款处理
            boolean refundSuccess = processRefund(payment, refundAmount, refundReason);
            
            if (refundSuccess) {
                // 更新支付状态为已退款
                paymentMapper.updateStatus(payment.getId(), 3);
                // 删除缓存
                deletePaymentCache(paymentNo);
                
                log.info("退款申请成功，支付流水号：{}，退款金额：{}", paymentNo, refundAmount);
                return Result.success("退款申请成功，预计1-3个工作日到账");
            } else {
                return Result.error("退款申请失败");
            }
        } catch (Exception e) {
            log.error("申请退款异常", e);
            return Result.error("申请退款失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> handleRefundCallback(String refundNo, Integer status) {
        try {
            // 这里处理退款回调逻辑
            String message = status == 1 ? "退款成功" : "退款失败";
            log.info("处理退款回调，退款流水号：{}，状态：{}，消息：{}", refundNo, status, message);
            return Result.success(message);
        } catch (Exception e) {
            log.error("处理退款回调异常", e);
            return Result.error("处理退款回调失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelPayment(String paymentNo) {
        try {
            Payment payment = paymentMapper.findByPaymentNo(paymentNo);
            if (payment == null) {
                return Result.error("支付记录不存在");
            }

            if (payment.getStatus() != 0) {
                return Result.error("只有待支付的订单才能取消");
            }

            // 更新支付状态为支付失败
            int result = paymentMapper.updateStatus(payment.getId(), 2);
            if (result > 0) {
                // 删除缓存
                deletePaymentCache(paymentNo);
                log.info("支付取消成功，支付流水号：{}", paymentNo);
                return Result.success("支付已取消");
            } else {
                return Result.error("取消支付失败");
            }
        } catch (Exception e) {
            log.error("取消支付异常", e);
            return Result.error("取消支付失败：" + e.getMessage());
        }
    }

    /**
     * 通过Feign调用获取真实订单号
     */
    private String getRealOrderNo(Long orderId) {
        try {
            log.info("开始通过Feign调用获取订单号，orderId：{}", orderId);
            Result<String> result = orderServiceClient.getOrderNoByOrderId(orderId);
            
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                String orderNo = result.getData();
                log.info("成功获取真实订单号：{}，orderId：{}", orderNo, orderId);
                return orderNo;
            } else {
                log.warn("Feign调用获取订单号失败，使用降级方案，orderId：{}，响应：{}", orderId, result);
                return getFallbackOrderNo(orderId);
            }
        } catch (Exception e) {
            log.error("Feign调用获取订单号异常，使用降级方案，orderId：{}", orderId, e);
            return getFallbackOrderNo(orderId);
        }
    }
    
    /**
     * 降级方案：生成默认订单号
     */
    private String getFallbackOrderNo(Long orderId) {
        String fallbackOrderNo = "FS" + orderId + System.currentTimeMillis();
        log.warn("使用降级订单号：{}，orderId：{}", fallbackOrderNo, orderId);
        return fallbackOrderNo;
    }

    /**
     * 生成支付流水号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 模拟第三方退款处理
     */
    private boolean processRefund(Payment payment, BigDecimal refundAmount, String refundReason) {
        // 模拟处理
        log.info("模拟退款处理，支付方式：{}，退款金额：{}", payment.getPaymentMethod(), refundAmount);
        return true;
    }

    /**
     * 转换为VO
     */
    private PaymentVO convertToVO(Payment payment) {
        PaymentVO paymentVO = new PaymentVO();
        BeanUtils.copyProperties(payment, paymentVO);
        
        // 设置支付方式名称
        paymentVO.setPaymentMethodName(getPaymentMethodName(payment.getPaymentMethod()));
        
        // 设置状态名称
        paymentVO.setStatusName(getStatusName(payment.getStatus()));
        
        return paymentVO;
    }

    /**
     * 获取支付方式名称
     */
    private String getPaymentMethodName(Integer paymentMethod) {
        return switch (paymentMethod) {
            case 1 -> "支付宝";
            case 2 -> "微信支付";
            case 3 -> "银行卡";
            default -> "未知";
        };
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "支付成功";
            case 2 -> "支付失败";
            case 3 -> "已退款";
            default -> "未知";
        };
    }

    /**
     * 缓存支付信息
     */
    private void cachePayment(Payment payment) {
        try {
            String key = PAYMENT_CACHE_KEY + payment.getPaymentNo();
            redisTemplate.opsForValue().set(key, payment, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("缓存支付信息失败", e);
        }
    }

    /**
     * 从缓存获取支付信息
     */
    private PaymentVO getPaymentFromCache(String paymentNo) {
        try {
            String key = PAYMENT_CACHE_KEY + paymentNo;
            Payment payment = (Payment) redisTemplate.opsForValue().get(key);
            if (payment != null) {
                return convertToVO(payment);
            }
        } catch (Exception e) {
            log.error("从缓存获取支付信息失败", e);
        }
        return null;
    }

    /**
     * 删除支付缓存
     */
    private void deletePaymentCache(String paymentNo) {
        try {
            String key = PAYMENT_CACHE_KEY + paymentNo;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除支付缓存失败", e);
        }
    }
    
    @Override
    public Result<List<PaymentVO>> getAllPaymentsForDebug() {
        try {
            log.info("调试：开始查询所有支付记录");
            
            // 直接查询数据库中的所有记录
            List<Payment> allPayments = paymentMapper.findAllForDebug();
            
            log.info("调试：查询到 {} 条支付记录", allPayments != null ? allPayments.size() : 0);
            
            if (allPayments == null || allPayments.isEmpty()) {
                return Result.error("没有找到任何支付记录");
            }
            
            // 转换为VO列表
            List<PaymentVO> result = allPayments.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("调试：查询所有支付记录异常", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取支付回调消息
     */
    private String getPaymentCallbackMessage(Integer status) {
        return switch (status) {
            case 1 -> "支付成功";
            case 2 -> "支付失败";
            case 0 -> "支付处理中";
            default -> "支付状态未知";
        };
    }
} 