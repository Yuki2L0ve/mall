package com.atguigu.gmall.payment.service;

import java.util.Map;

/**
 * 微信支付的接口类
 */
public interface WxPayService {

    /**
     * 获取微信支付的二维码地址
     *
     * @param payParams
     * @return
     */
    public Map<String, String> getPayCode(Map<String, String> payParams);

    /**
     * 查询指定订单的支付结果
     * @param orderId
     * @return
     */
    public Map<String, String> getPayResult(String orderId);

    /**
     * 关闭交易
     * @param orderId
     * @return
     */
    public Map<String, String> closePayUrl(String orderId);
}
