package com.atguigu.gmall.order.listener;

import com.atguigu.gmall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听订单超时消息的消费者
 */
@Component
@Log4j2
public class OrderTimeoutListener {

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 监听超时订单的消息
     *
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "order_nomal_queue")
    public void orderTimeout(Message message, Channel channel) {
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息的内容
        Long orderId = Long.parseLong(new String(message.getBody()));
        try {
            //超时取消订单
            orderInfoService.cancelOrder(orderId);
            //确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                //消息消费失败出现异常,判断消息是否第一次被消费
                if (messageProperties.getRedelivered()) {
                    e.printStackTrace();
                    //第二次, 记录日志,从队列移除
                    log.error("超时取消订单失败,订单的id为: " + orderId);
                    channel.basicReject(deliveryTag, false);
                } else {
                    //第一次: 拒绝消费,放回队列
                    channel.basicReject(deliveryTag, true);
                }
            } catch (Exception e1) {
                log.error("超时取消订单拒绝消息失败, 失败的订单id为:" + orderId + ",失败的原因为:" + e1.getMessage());
            }
        }
    }
}
