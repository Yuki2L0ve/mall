package com.atguigu.gmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * 商品详情微服务的启动类
 */
// 在 @SpringBootApplication 注解中使用 exclude 属性，允许开发者指定不希望被自动配置的类。
// DataSourceAutoConfiguration.class 是 Spring Boot 自动配置数据源的配置类。
// 当你在 @SpringBootApplication 注解中使用 exclude = DataSourceAutoConfiguration.class 时，你告诉 Spring Boot 不要自动配置数据源。
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
// @EnableFeignClients：这是一个Spring Boot的注解，用于启用Feign客户端的支持。
// 当你在Spring Boot应用的主类或者配置类上添加这个注解时，它会告诉Spring Boot自动扫描并加载所有带有@FeignClient注解的接口。
// basePackages：这是@EnableFeignClients注解的一个属性，用于指定要扫描的包。
// 在这个例子中，表示Spring Boot将扫描com.atguigu.gmall.product.feign这个包下的所有类，查找并加载所有带有@FeignClient注解的接口。
// com.atguigu.gmall.product.feign：这是一个包路径，表示你的Feign客户端接口应该位于这个包或者其子包下。
// 在这个包中，你可以定义多个Feign客户端接口，每个接口都代表一个微服务的客户端。
@EnableFeignClients(basePackages = "com.atguigu.gmall.product.feign")
@ComponentScan("com.atguigu.gmall")
public class ItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }
}
