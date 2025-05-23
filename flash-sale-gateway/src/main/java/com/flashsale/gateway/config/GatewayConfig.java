package com.flashsale.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关配置
 * @author 21311
 */
@Configuration
public class GatewayConfig {

    /**
     * 网关路由
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User service
                .route("user-service", r -> r.path("/api/user/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-user"))
                
                // Product service
                .route("product-service", r -> r.path("/api/product/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-product"))
                
                // Flash sale service
                .route("seckill-service", r -> r.path("/api/seckill/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-seckill"))
                
                // Order service
                .route("order-service", r -> r.path("/api/order/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-order"))
                
                // Payment service
                .route("payment-service", r -> r.path("/api/payment/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-payment"))
                
                // Coupon service
                .route("coupon-service", r -> r.path("/api/coupon/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-coupon"))
                
                .build();
    }
} 