package com.example.mall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 购物车微服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan("com.example.mall")
@EnableFeignClients(basePackages = "com.example.mall.product.feign")
@ServletComponentScan("com.example.mall.cart.filter")
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
