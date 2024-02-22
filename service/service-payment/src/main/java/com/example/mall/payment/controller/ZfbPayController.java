package com.example.mall.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.mall.payment.service.ZfbPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/zfb/pay")
public class ZfbPayController {
    @Resource
    private ZfbPayService zfbPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取支付宝支付的页面
     * @param orderId
     * @param money
     * @param desc
     * @return
     */
    @GetMapping("/getPayPage")
    public String getPayPage(String orderId, String money, String desc) {
        return zfbPayService.getPayPage(orderId, money, desc);
    }

    /**
     * 获取支付宝订单的支付结果
     * @param orderId
     * @return
     */
    @GetMapping("/getPayResult")
    public String getPayResult(String orderId) {
        return zfbPayService.getPayResult(orderId);
    }

    /**
     * 同步回调：用户在支付宝那边付完钱以后，跳转回来的地方
     * @return
     */
    @RequestMapping("/callback/return")
    public String callbackReturn(@RequestParam Map<String, String> returnMap) {
        return "支付宝支付完成，跳转回到商城里面来了！";
    }

    /**
     * 异步通知地址：用户支付完成以后，支付宝通知商城支付结果的接口
     * @param notifyMap
     * @return
     */
    @RequestMapping("/callback/notify")
    public String callbackNotify(@RequestParam Map<String, String> notifyMap) {
        String result = "{\"gmt_create\":\"2023-07-18 23:25:44\",\"charset\":\"utf-8\",\"gmt_payment\":\"2023-07-18 23:25:52\",\"notify_time\":\"2023-07-18 23:25:53\",\"subject\":\"梅旺旺的大玩具哦哦哦\",\"sign\":\"fzp+F/Q+CHwVXNj5/oFNEw+nipxQve0r1xaXL4PF3ByCqeNpCsl1mt+fRzlCatIytOPd8q/ldsa/vCNHtI/JQXuKT0qFBL0hpBYwD83LNyRogqtzAGb0NnZIBLyLueOhbLwB3SrcDTuBUIl4FCPT/WQUdis1rRpRdGiHIfLM02OSQWh22xzO8ChsLDpDaImturVSlcPk6pw0wwuu1ECTsO52ZqScwNwA0oJZiiRCqP5KtJe4glQ6XbRpTkesX9RUrWiWXlBNVoiORHlvg+VFoaI7cT1Qckco14Uqbm7Rav5ybgPQAgl5UXkJg79BlZdgi7P3Or52raxavLcGtn9bHw==\",\"buyer_id\":\"2088622697221629\",\"invoice_amount\":\"0.01\",\"version\":\"1.0\",\"notify_id\":\"2023071801222232552021621448352363\",\"fund_bill_list\":\"[{\\\"amount\\\":\\\"0.01\\\",\\\"fundChannel\\\":\\\"ALIPAYACCOUNT\\\"}]\",\"notify_type\":\"trade_status_sync\",\"out_trade_no\":\"256\",\"total_amount\":\"0.01\",\"trade_status\":\"TRADE_SUCCESS\",\"trade_no\":\"2023071822001421621401431731\",\"auth_app_id\":\"2021001163617452\",\"receipt_amount\":\"0.01\",\"point_amount\":\"0.00\",\"buyer_pay_amount\":\"0.01\",\"app_id\":\"2021001163617452\",\"sign_type\":\"RSA2\",\"seller_id\":\"2088831489324244\"}";
        rabbitTemplate.convertAndSend("pay_exchange", "pay.order", result);
        return "success";
    }
}
