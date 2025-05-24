package com.flashsale.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 商品服务启动类
 * @author 21311
 */
@SpringBootApplication(exclude = {RabbitAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.flashsale.product.mapper")
@ComponentScan(basePackages = {"com.flashsale.product", "com.flashsale.common"})
public class ProductApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}