package com.example.mall.order.controller;

import com.example.mall.common.constant.OrderConst;
import com.example.mall.common.result.Result;
import com.example.mall.order.service.UserPayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 订单支付相关的控制层
 */
@RestController
@RequestMapping("/api/order")
public class UserPayController {
    @Resource
    private UserPayService userPayService;

    /**
     * 在用户选择某个支付渠道后调用的方法  微信
     * @param orderId
     * @return
     */
    @GetMapping("/getWxPayAddress")
    public String getWxPayAddress(Long orderId) {
        return userPayService.getOrderPayUrl(orderId, OrderConst.WX_PAY);
    }

    /**
     * 在用户选择某个支付渠道后调用的方法    支付宝
     * @param orderId
     * @return
     */
    @GetMapping("/getZfbPayAddress")
    public String getZfbPayAddress(Long orderId) {
        return userPayService.getOrderPayUrl(orderId, OrderConst.ZFB_PAY);
    }
}
