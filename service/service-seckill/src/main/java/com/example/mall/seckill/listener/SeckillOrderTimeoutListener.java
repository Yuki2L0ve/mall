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
 * 监听秒杀订单超时未支付消息的消费者
 */
@Component
@Log4j2
public class SeckillOrderTimeoutListener {


    @Autowired
    private SeckillOrderService seckillOrderService;
    /**
     * 将收到消息时候依然未支付的秒杀订单取消掉
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "seckill_order_nomal_queue")
    public void seckillOrderTimeout(Message message, Channel channel){
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息的内容
        String username = new String(message.getBody());
        try {
            //超时取消订单
            seckillOrderService.cancelSeckillOrder(username);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                if(messageProperties.getRedelivered()){
                    //第二次
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("取消秒杀超时订单失败,失败的用户为:" + username + ",失败的原因为:" + e1.getMessage());
            }
        }
    }
}
