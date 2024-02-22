package com.example.mall.payment.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 可靠性投递的配置
 */
@Component
@Log4j2
public class RabbitRetrunAndComfirmConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 定义初始化的方案,在项目扫描到这个类的时候,就配置好RabbitTemplate中的两种模式
     */
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this::confirm);
        rabbitTemplate.setReturnCallback(this::returnedMessage);
    }

    /**
     * 确认消息进入交换机
     * @param correlationData
     * @param b
     * @param s
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
     * 确认消息没有进入队列
     * @param message
     * @param i
     * @param s
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
