package com.atguigu.gmall.order.config;

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
     * 死信队列 是RabbitMQ中的一个特殊队列，用于存储无法被正常路由的消息，或者在一定条件下被拒绝的消息。
     *
     * @return
     */
    @Bean("orderDeadQueue")
    public Queue orderDeadQueue(){
        // QueueBuilder是Spring AMQP提供的一个工具类，用于构建队列的配置
        return QueueBuilder
                // 这个方法调用指定队列的名称为"order_dead_queue"，并且设置队列为持久化。持久化队列意味着即使RabbitMQ服务器重启，队列也会被保留。
                .durable("order_dead_queue")
                // 这个方法调用为队列添加了一个参数"x-dead-letter-exchange"，它的值是"order_dead_exchange"。
                // 这个参数指定了当消息成为死信时，应该发送到的交换机名称。死信交换机（DLX）用于接收死信消息，并根据路由键将它们路由到另一个队列。
                .withArgument("x-dead-letter-exchange", "order_dead_exchange")
                // 这个方法调用为队列添加了另一个参数"x-dead-letter-routing-key"，它的值是"order.dead"。
                // 这个参数指定了死信消息的路由键。当消息被发送到死信交换机时，这个路由键决定了消息应该被路由到哪个队列。
                .withArgument("x-dead-letter-routing-key", "order.dead")
                // 这个方法调用构建并返回队列的配置。
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
