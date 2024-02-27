package com.atguigu.gmall.payment.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 可靠性投递的配置 它实现了RabbitMQ的可靠性投递配置。
 * 这个组件通过实现RabbitTemplate.ConfirmCallback和RabbitTemplate.ReturnCallback接口，提供了消息确认和消息返回的回调方法。
 * 通过这种方式，RabbitRetrunAndComfirmConfig类可以监控消息的发送过程，确保消息能够成功送达RabbitMQ。
 * 如果消息发送失败，可以通过日志来诊断问题，这对于确保消息传递的可靠性非常重要。
 */
@Component
@Log4j2
public class RabbitRetrunAndComfirmConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 定义初始化的方案,在项目扫描到这个类的时候,就配置好RabbitTemplate中的两种模式
     * @PostConstruct这个注解表明init方法会在依赖注入完成后执行。这是一个初始化方法，用于配置RabbitTemplate的确认和返回回调。
     */
    @PostConstruct
    public void init(){
        // 设置RabbitTemplate的确认回调
        rabbitTemplate.setConfirmCallback(this::confirm);
        // 设置RabbitTemplate的返回回调
        rabbitTemplate.setReturnCallback(this::returnedMessage);
    }

    /**
     * 确认消息进入交换机    这是RabbitTemplate.ConfirmCallback接口的实现方法，用于确认消息是否成功进入交换机。
     *
     * @param correlationData   correlationData是与消息关联的数据，可以用来匹配发送和确认的消息。
     * @param b b是一个布尔值，表示消息是否成功进入交换机。
     * @param s s是交换机的名称。
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if(!b){
            log.error("支付结果通知失败没有抵达交换机,失败的原因为:" +
                    s +
                    ", 通知消息的元数据为:" + JSONObject.toJSONString(correlationData));
        }
    }

    /**
     * 确认消息没有进入队列   这是RabbitTemplate.ReturnCallback接口的实现方法，用于处理消息返回的情况。
     * @param message   message是返回的消息对象。
     * @param i         i是消息的回复码。
     * @param s     s、s1和s2是与消息返回相关的字符串信息。
     * @param s1
     * @param s2
     */
    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        log.error("支付结果通知失败抵达了交换机但是没有抵达队列,失败的原因为:" +
                s
                + ", 通知消息的具体内容为:" + new String(message.getBody()));
    }
}
