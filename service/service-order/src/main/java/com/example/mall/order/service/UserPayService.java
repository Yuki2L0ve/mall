package com.example.mall.order.service;

/**
 * 记录用户的支付渠道和信息的接口
 */
public interface UserPayService {

    /**
     *
     * @param orderId
     * @param payway  微信 或 支付宝
     * @return
     */
    public String getOrderPayUrl(Long orderId, String payway);
}
