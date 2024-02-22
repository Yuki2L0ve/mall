package com.example.mall.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付微服务使用通知支付结果的消息队列的配置
 */
@Configuration
public class PaymentRabbitConfig {

    /**
     * 创建交换机
     */
    @Bean("payExchange")
    public Exchange payExchange(){
        return ExchangeBuilder.directExchange("pay_exchange").build();
    }

    /**
     * 普通订单的支付结果队列
     * @return
     */
    @Bean("orderPayQueue")
    public Queue orderPayQueue(){
        return QueueBuilder.durable("order_pay_queue").build();
    }

    /**
     * 创建秒杀订单的支付结果队列
     * @return
     */
    @Bean("seckillOrderPayQueue")
    public Queue seckillOrderPayQueue(){
        return QueueBuilder.durable("seckill_order_pay_queue").build();
    }

    /**
     * 秒杀订单队列和交换绑定
     * @param payExchange
     * @param seckillOrderPayQueue
     * @return
     */
    @Bean
    public Binding seckillOrderPayBinding(@Qualifier("payExchange") Exchange payExchange,
                                          @Qualifier("seckillOrderPayQueue") Queue seckillOrderPayQueue){
        return BindingBuilder.bind(seckillOrderPayQueue).to(payExchange).with("seckill.pay.order").noargs();
    }

    /**
     * 普通订单的交换机和队列绑定
     * @param payExchange
     * @param orderPayQueue
     * @return
     */
    @Bean
    public Binding orderPayBinding(@Qualifier("payExchange") Exchange payExchange,
                                   @Qualifier("orderPayQueue") Queue orderPayQueue){
        return BindingBuilder.bind(orderPayQueue).to(payExchange).with("pay.order").noargs();
    }
}
