package com.flashsale.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 网关配置类 - 支持跨域、路由等功能
 * @author 21311
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * 网关路由配置
     * @author 21311
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        log.info("正在配置网关路由...");
        return builder.routes()
                // 用户服务
                .route("user-service", r -> r.path("/api/user/**")
                        .uri("lb://flash-sale-user"))
                //未来扩容可以进行负载均衡
                
                // 商品服务
                .route("product-service", r -> r.path("/api/product/**")
                        .uri("lb://flash-sale-product"))
                
                // 秒杀服务
                .route("seckill-service", r -> r.path("/api/seckill/**")
                        .uri("lb://flash-sale-seckill"))
                
                // 订单服务
                .route("order-service", r -> r.path("/api/order/**")
                        .uri("lb://flash-sale-order"))
                
                // 支付服务
                .route("payment-service", r -> r.path("/api/payment/**")
                        .uri("lb://flash-sale-payment"))
                
                .build();
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        //允许携带凭据
        corsConfiguration.addAllowedOriginPattern("*");
        //允许来源
        corsConfiguration.addAllowedHeader("*");
        //允许请求头
        corsConfiguration.addAllowedMethod("*");
        //允许HTTP方法
        corsConfiguration.setMaxAge(3600L);
        //非简单请求的预检请求缓存时间（单位：秒）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        //将规则绑定到/**路径

        return new CorsWebFilter(source);
    }
} 