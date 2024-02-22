package com.example.mall.list.listener;

import com.example.mall.list.service.GoodsService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听商品上下架同步消息
 */
@Component
@Log4j2
public class SkuUpperOrDownListener {

    @Autowired
    private GoodsService goodsService;

    /**
     * 监听上架消息
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "sku_upper_queue")
    public void skuUpper(Message message, Channel channel){
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息的内容
        Long skuId = Long.parseLong(new String(message.getBody()));
        try {
            //完成上架
            goodsService.dbSkuAddIntoEs(skuId);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //消息消费失败出现异常,判断消息是否第一次被消费
                if(messageProperties.getRedelivered()){
                    e.printStackTrace();
                    //第二次, 记录日志,从队列移除
                    log.error("商品上架失败,商品的id为: " + skuId);
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次: 拒绝消费,放回队列
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("商品上架拒绝消息失败, 失败的商品id为:" + skuId + ",失败的原因为:" + e1.getMessage());
            }
        }
    }

    /**
     * 监听商品下架消息
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "sku_down_queue")
    public void skuDown(Message message, Channel channel){
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息的内容
        Long skuId = Long.parseLong(new String(message.getBody()));
        try {
            //完成下架
            goodsService.removeGoodsFromEs(skuId);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //消息消费失败出现异常,判断消息是否第一次被消费
                if(messageProperties.getRedelivered()){
                    //第二次, 记录日志,从队列移除
                    log.error("商品下架失败,商品的id为: " + skuId);
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次: 拒绝消费,放回队列
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("商品下架拒绝消息失败, 失败的商品id为:" + skuId + ",失败的原因为:" + e1.getMessage());
            }
        }
    }
}