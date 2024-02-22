package com.example.mall.seckill.listener;

import com.example.mall.seckill.service.SeckillOrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听秒杀订单支付结果的消息
 */
@Component
@Log4j2
public class SeckillOrderPayListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 监听秒杀订单支付的消息,修改订单的支付结果
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "seckill_order_pay_queue")
    public void seckillOrderPay(Message message, Channel channel){
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息的内容
        String s = new String(message.getBody());
        try {
            //秒杀订单支付结果修改
            seckillOrderService.updateSeckillOrder(s);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //只尝试一次
                channel.basicReject(deliveryTag, false);
            }catch (Exception e1){
                log.error("秒杀订单支付修改失败, 报文为:" + s + ",失败的原因为:" + e1.getMessage());
            }
        }
    }
}
