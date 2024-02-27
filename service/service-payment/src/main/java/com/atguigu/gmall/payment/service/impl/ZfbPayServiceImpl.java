package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.payment.service.ZfbPayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付宝相关的支付接口实现类
 */
@Service
public class ZfbPayServiceImpl implements ZfbPayService {
    @Value("${alipay_url}")
    private String alipayUrl;

    @Value("${app_id}")
    private String appId;

    @Value("${app_private_key}")
    private String appPrivateKey;

    @Value("${alipay_public_key}")
    private String alipayPublicKey;

    @Value("${return_payment_url}")
    private String returnPaymentUrl;

    @Value("${notify_payment_url}")
    private String notifyPaymentUrl;

    /**
     * 获取支付宝支付的页面地址
     *
     * @param orderId
     * @param money
     * @param desc
     * @return
     */
    @Override
    public String getPayPage(String orderId, String money, String desc) {
        //支付宝支付的客户端对象初始化(连接初始化)
        AlipayClient alipayClient =
                new DefaultAlipayClient(alipayUrl,
                        appId,
                        appPrivateKey,
                        "json",
                        "utf-8",
                        alipayPublicKey,
                        "RSA2");
        //请求体对象初始化
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        //用户支付完钱以后,去哪里通知商城支付结果(引导商城)
        request.setNotifyUrl(notifyPaymentUrl);
        //用户支付完成以后,跳转到哪里去(引导用户)
        request.setReturnUrl(returnPaymentUrl);
        //包装请求参数
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "java0509000000" + orderId);
        bizContent.put("total_amount", money);
        bizContent.put("subject", desc);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        //设置请求参数
        request.setBizContent(bizContent.toString());
        //发起请求
        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if(response.isSuccess()){
                return response.getBody();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询指定订单的支付结果
     *
     * @param orderId
     * @return
     */
    @Override
    public String getPayResult(String orderId) {
        //支付宝支付的客户端对象初始化(连接初始化)
        AlipayClient alipayClient =
                new DefaultAlipayClient(alipayUrl,
                        appId,
                        appPrivateKey,
                        "json",
                        "utf-8",
                        alipayPublicKey,
                        "RSA2");
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "java0509000000" + orderId);
        request.setBizContent(bizContent.toString());
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if(response.isSuccess()){
                return response.getBody();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
