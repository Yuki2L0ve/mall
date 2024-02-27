package com.atguigu.gmall.seckill.listener;

import com.atguigu.gmall.seckill.service.SeckillGoodsService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听秒杀商品活动到期后同步商品库存数据的消费者
 */
@Component
@Log4j2
public class SeckillGoodsStockToDbListener {

    @Autowired
    private SeckillGoodsService seckillGoodsService;

    /**
     * 将秒杀商品活动结束后剩余的库存数据同步到数据库中去
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "seckill_goods_nomal_queue")
    public void seckillGoodsStockToDb(Message message, Channel channel){
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息的内容
        String time = new String(message.getBody());
        try {
            //同步数据到数据库中去
            seckillGoodsService.updateSeckillGoodsStock(time);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                if(messageProperties.getRedelivered()){
                    log.error("秒杀商品剩余库存同步到数据库失败,失败的时间段为:" + time + ",失败的原因为:" + e.getMessage());
                    //第二次
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("秒杀商品剩余库存同步到数据库失败,失败的时间段为:" + time + ",失败的原因为:" + e1.getMessage());
            }
        }
    }
}
