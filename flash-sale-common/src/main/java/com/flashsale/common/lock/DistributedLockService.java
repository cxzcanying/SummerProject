package com.flashsale.common.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁服务接口
 * @author 21311
 */
public interface DistributedLockService {
    
    /**
     * 尝试获取锁
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit);
    
    /**
     * 释放锁
     * @param lockKey 锁的key
     */
    void unlock(String lockKey);
    
    /**
     * 执行带锁的业务逻辑
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 锁持有时间
     * @param timeUnit 时间单位
     * @param task 业务逻辑
     * @return 执行结果
     */
    <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit, LockCallback<T> task);
    
    /**
     * 锁回调接口
     */
    @FunctionalInterface
    interface LockCallback<T> {
        T execute() throws Exception;
    }
} 