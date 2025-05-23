package com.flashsale.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 订单服务启动类
 * @author 21311
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.flashsale.order.mapper")
@ComponentScan(basePackages = {"com.flashsale.order", "com.flashsale.common"})
public class OrderApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
} 