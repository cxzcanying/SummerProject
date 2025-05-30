package com.flashsale.seckill.config;

import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sentinel配置类 - 仅秒杀模块
 * @author 21311
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * 注册Sentinel注解切面
     */
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        log.info("✅ Sentinel切面配置完成 - 仅秒杀模块");
        return new SentinelResourceAspect();
    }
} 