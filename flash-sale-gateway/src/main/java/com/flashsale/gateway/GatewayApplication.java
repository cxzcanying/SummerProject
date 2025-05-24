package com.flashsale.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Gateway application
 * @author 21311
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.flashsale")
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
} 