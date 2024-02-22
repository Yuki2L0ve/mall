package com.example.mall.order.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单超时消息的配置类
 */
@Configuration
public class OrderDelayRabbitConfig{

    /**
     * 创建正常的交换机
     */
    @Bean("orderNomalExchange")
    public Exchange orderNomalExchange(){
        return ExchangeBuilder.directExchange("order_nomal_exchange").build();
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("orderDeadQueue")
    public Queue orderDeadQueue(){
        return QueueBuilder
                .durable("order_dead_queue")
                .withArgument("x-dead-letter-exchange", "order_dead_exchange")
                .withArgument("x-dead-letter-routing-key", "order.dead")
                .build();
    }

    /**
     * 正常交换机和死信队列绑定
     * @param orderNomalExchange
     * @param orderDeadQueue
     * @return
     */
    @Bean
    public Binding nomalBinding(@Qualifier("orderNomalExchange") Exchange orderNomalExchange,
                                @Qualifier("orderDeadQueue") Queue orderDeadQueue){
        return BindingBuilder.bind(orderDeadQueue).to(orderNomalExchange).with("order.nomal").noargs();
    }

    /**
     * 创建死信的交换机
     */
    @Bean("orderDeadExchange")
    public Exchange orderDeadExchange(){
        return ExchangeBuilder.directExchange("order_dead_exchange").build();
    }

    /**
     * 正常队列
     * @return
     */
    @Bean("orderNomalQueue")
    public Queue orderNomalQueue(){
        return QueueBuilder.durable("order_nomal_queue").build();
    }

    /**
     * 死信交换机和正常队列绑定
     * @param orderDeadExchange
     * @param orderNomalQueue
     * @return
     */
    @Bean
    public Binding deadBinding(@Qualifier("orderDeadExchange") Exchange orderDeadExchange,
                               @Qualifier("orderNomalQueue") Queue orderNomalQueue){
        return BindingBuilder.bind(orderNomalQueue).to(orderDeadExchange).with("order.dead").noargs();
    }

}
