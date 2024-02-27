package com.atguigu.gmall.payment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * 支付微服务提供内部调用的feign模块
 */
@FeignClient(name = "service-payment", path = "/wx/pay", contextId = "paymentWxFeign")
public interface PaymentWxFeign {
    /**
     * 获取微信支付的二维码地址
     * @param orderId
     * @param money
     * @param desc
     * @return
     */
    @GetMapping("/getPayCodeUrl")
    public Map<String, String> getPayCodeUrl(@RequestParam String orderId,
                                             @RequestParam String money,
                                             @RequestParam String desc);

    /**
     *  获取订单的支付结果
     * @param orderId
     * @return
     */
    @GetMapping("/getPayResult")
    public Map<String, String> getPayResult(@RequestParam String orderId);

    /**
     *  关闭交易
     * @param orderId
     * @return
     */
    @GetMapping("/closePayUrl")
    public Map<String, String> closePayUrl(@RequestParam String orderId);
}
