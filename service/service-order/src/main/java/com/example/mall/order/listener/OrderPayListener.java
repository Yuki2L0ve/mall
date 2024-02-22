package com.example.mall.order.listener;

import com.example.mall.order.service.OrderInfoService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听订单支付结果的消息:在订单支付完成后,第三方会通知支付结果
 */
@Component
@Log4j2
public class OrderPayListener {

    @Autowired
    private OrderInfoService orderInfoService;

    /**
     * 监听订单的支付消息,修改订单的状态: 已支付
     * @param message
     * @param channel
     */
    @RabbitListener(queues = "order_pay_queue")
    public void orderPay(Message message, Channel channel){
        //获取消息的属性
        MessageProperties messageProperties = message.getMessageProperties();
        //获取消息的编号
        Long deliveryTag = messageProperties.getDeliveryTag();
        //获取消息的内容: 支付的结果: json
        String result = new String(message.getBody());
        try {
            //修改订单的状态为:已支付
            orderInfoService.updateOrder(result);
            //确认消息
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            try {
                //消息消费失败出现异常,判断消息是否第一次被消费
                if(messageProperties.getRedelivered()){
                    e.printStackTrace();
                    //第二次, 记录日志,从队列移除
                    log.error("修改订单的支付结果失败,支付结果的详细内容为: " + result);
                    channel.basicReject(deliveryTag, false);
                }else{
                    //第一次: 拒绝消费,放回队列
                    channel.basicReject(deliveryTag, true);
                }
            }catch (Exception e1){
                log.error("修改订单的支付结果失败, 支付结果的详细内容为:" + result + ",失败的原因为:" + e1.getMessage());
            }
        }
    }
}
