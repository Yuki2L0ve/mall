package com.example.mall.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 秒杀下单排队消息的消息队列配置
 */
@Configuration
public class SeckillOrderRabbitConfig {

    /**
     * 创建交换机
     * @return
     */
    @Bean("seckillOrderExchange")
    public Exchange seckillOrderExchange(){
        return ExchangeBuilder.directExchange("seckill_order_exchange").build();
    }

    /**
     * 创建队列
     * @return
     */
    @Bean("seckillOrderQueue")
    public Queue seckillOrderQueue(){
        return QueueBuilder.durable("seckill_order_queue").build();
    }

    /**
     * 交换机队列绑定
     * @param seckillOrderQueue
     * @param seckillOrderExchange
     * @return
     */
    @Bean
    public Binding seckillOrderBinding(@Qualifier("seckillOrderQueue") Queue seckillOrderQueue,
                                       @Qualifier("seckillOrderExchange") Exchange seckillOrderExchange){
        return BindingBuilder.bind(seckillOrderQueue).to(seckillOrderExchange).with("seckill.order.add").noargs();
    }
}
