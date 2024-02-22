package com.example.mall.product.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商品上架下架和es数据同步的消息队列配置
 */
@Configuration
public class ProductRabbitConfig {

    /**
     * 上下架使用的交换机
     * @return
     */
    @Bean("productExchange")
    public Exchange productExchange(){
        return ExchangeBuilder.directExchange("product_exchange").build();
    }

    /**
     * 商品上架消息队列
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
     * 上架队列绑定
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
     * 下架队列绑定
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
