package com.example.mall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 秒杀订单延迟队列用于取消超时未支付的订单
 */
@Configuration
public class SeckillOrderDelayRabbitConfig {

    /**
     * 正常交换机
     * @return
     */
    @Bean("seckillOrderNomalExchange")
    public Exchange seckillOrderNomalExchange(){
        return ExchangeBuilder.directExchange("seckill_order_nomal_exchange").build();
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("seckillOrderDeadQueue")
    public Queue seckillOrderDeadQueue(){
        return QueueBuilder
                .durable("seckill_order_dead_queue")
                .withArgument("x-dead-letter-exchange", "seckill_order_dead_exchange")
                .withArgument("x-dead-letter-routing-key", "seckill.order.nomal")
                .build();
    }

    /**
     * 正常交换机和死信队列绑定
     * @param seckillOrderNomalExchange
     * @param seckillOrderDeadQueue
     * @return
     */
    @Bean
    public Binding seckillOrderDeadBinding(@Qualifier("seckillOrderNomalExchange") Exchange seckillOrderNomalExchange,
                                           @Qualifier("seckillOrderDeadQueue") Queue seckillOrderDeadQueue){
        return BindingBuilder.bind(seckillOrderDeadQueue).to(seckillOrderNomalExchange).with("seckill.order.dead").noargs();
    }

    /**
     * 死信交换机
     * @return
     */
    @Bean("seckillOrderDeadExchange")
    public Exchange seckillOrderDeadExchange(){
        return ExchangeBuilder.directExchange("seckill_order_dead_exchange").build();
    }

    /**
     * 死信队列
     * @return
     */
    @Bean("seckillOrderNomalQueue")
    public Queue seckillOrderNomalQueue(){
        return QueueBuilder.durable("seckill_order_nomal_queue").build();
    }

    /**
     * 死信交换机和正常队列绑定
     * @param seckillOrderDeadExchange
     * @param seckillOrderNomalQueue
     * @return
     */
    @Bean
    public Binding seckillOrderNomalBinding(@Qualifier("seckillOrderDeadExchange") Exchange seckillOrderDeadExchange,
                                            @Qualifier("seckillOrderNomalQueue") Queue seckillOrderNomalQueue){
        return BindingBuilder.bind(seckillOrderNomalQueue).to(seckillOrderDeadExchange).with("seckill.order.nomal").noargs();
    }
}
