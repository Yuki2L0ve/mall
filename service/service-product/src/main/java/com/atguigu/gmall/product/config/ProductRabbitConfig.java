package com.atguigu.gmall.product.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商品上架下架和es数据同步的消息队列配置
 * 这个配置类用于定义和配置RabbitMQ消息队列和交换机，这些队列和交换机用于处理商品上架和下架的消息同步到Elasticsearch。
 */
@Configuration
public class ProductRabbitConfig {

    /**
     * 上下架使用的交换机
     * 这个注解表明productExchange方法返回的是一个Bean，它在Spring容器中的名称是productExchange。
     * 方法创建了一个直接交换机（directExchange），名为product_exchange。
     * 直接交换机根据路由键（routing key）将消息路由到一个或多个绑定的队列。
     * @return
     */
    @Bean("productExchange")
    public Exchange productExchange(){
        return ExchangeBuilder.directExchange("product_exchange").build();
    }

    /**
     * 商品上架消息队列
     * 这个注解表明skuUpperQueue方法返回的是一个Bean，它在Spring容器中的名称是skuUpperQueue。
     * QueueBuilder.durable方法创建了一个持久化队列，这意味着队列在RabbitMQ服务器重启后仍然存在。
     * @return
     */
    @Bean("skuUpperQueue")
    public Queue skuUpperQueue(){
        return QueueBuilder.durable("sku_upper_queue").build();
    }

    /**
     * 商品下架消息队列
     * @return
     */
    @Bean("skuDownQueue")
    public Queue skuDownQueue(){
        return QueueBuilder.durable("sku_down_queue").build();
    }

    /**
     * 上架队列绑定       将队列绑定到交换机
     * upperBinding方法将sku_upper_queue队列绑定到product_exchange交换机，并设置路由键为sku.upper。
     * 这意味着当发送到product_exchange交换机的消息具有sku.upper路由键时，它将被路由到sku_upper_queue队列。
     * @param skuUpperQueue
     * @param productExchange
     * @return
     */
    @Bean
    public Binding upperBinding(@Qualifier("skuUpperQueue") Queue skuUpperQueue,
                                @Qualifier("productExchange") Exchange productExchange){
        return BindingBuilder.bind(skuUpperQueue).to(productExchange).with("sku.upper").noargs();
    }

    /**
     * 下架队列绑定       用于将队列绑定到交换机
     * downBinding方法将sku_down_queue队列绑定到product_exchange交换机，并设置路由键为sku.down。
     * 这意味着当发送到product_exchange交换机的消息具有sku.down路由键时，它将被路由到sku_down_queue队列。
     * @param skuDownQueue
     * @param productExchange
     * @return
     */
    @Bean
    public Binding downBinding(@Qualifier("skuDownQueue") Queue skuDownQueue,
                               @Qualifier("productExchange") Exchange productExchange){
        return BindingBuilder.bind(skuDownQueue).to(productExchange).with("sku.down").noargs();
    }
}
