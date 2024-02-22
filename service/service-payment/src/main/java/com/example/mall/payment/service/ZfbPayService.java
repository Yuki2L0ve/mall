package com.example.mall.payment.service;

import java.util.Map;

/**
 * 支付宝相关的支付接口
 */
public interface ZfbPayService {
    /**
     * 获取支付宝支付的页面地址
     *
     * @param orderId
     * @param money
     * @param desc
     * @return
     */
    public String getPayPage(String orderId, String money, String desc);

    /**
     * 查询指定订单的支付结果
     * @param orderId
     * @return
     */
    public String getPayResult(String orderId);
}
