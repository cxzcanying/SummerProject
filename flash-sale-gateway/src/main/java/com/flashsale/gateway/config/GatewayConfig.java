package com.flashsale.gateway.config;

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
@Configuration
public class GatewayConfig {

    /**
     * 网关路由配置 - 移除Redis限流依赖
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User service - 用户服务
                .route("user-service", r -> r.path("/api/user/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-user"))
                
                // Product service - 商品服务
                .route("product-service", r -> r.path("/api/product/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-product"))
                
                // Seckill service - 秒杀服务
                .route("seckill-service", r -> r.path("/api/seckill/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-seckill"))
                
                // Order service - 订单服务
                .route("order-service", r -> r.path("/api/order/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-order"))
                
                // Payment service - 支付服务
                .route("payment-service", r -> r.path("/api/payment/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-payment"))
                
                // Coupon service - 优惠券服务
                .route("coupon-service", r -> r.path("/api/coupon/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://flash-sale-coupon"))
                
                .build();
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
} 