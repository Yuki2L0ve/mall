package com.example.mall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.mall.payment.service.WxPayService;
import com.example.mall.payment.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付的接口实现类
 */
@Service
public class WxPayServiceImpl implements WxPayService {
    @Value("${weixin.pay.appid}")
    private String appId;

    @Value("${weixin.pay.partner}")
    private String partner;

    @Value("${weixin.pay.partnerkey}")
    private String partnerkey;

    @Value("${weixin.pay.notifyUrl}")
    private String notifyUrl;

    /**
     * 获取微信支付的二维码地址
     *
     * @param payParams
     * @return
     */
    @Override
    public Map<String, String> getPayCode(Map<String, String> payParams) {
        // 参数校验
        if (StringUtils.isEmpty(payParams.get("orderId"))
                || StringUtils.isEmpty(payParams.get("money"))
                || StringUtils.isEmpty(payParams.get("desc"))) {
            return null;
        }
        try {
            // 定义微信统一下单接口的URL
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            // 包装请求参数：XML数据
            Map<String, String> mapParam = new HashMap<>();
            mapParam.put("appid", appId);
            mapParam.put("mch_id", partner);
            mapParam.put("nonce_str", WXPayUtil.generateNonceStr());
            mapParam.put("body", payParams.get("desc"));
            mapParam.put("out_trade_no", payParams.get("orderId"));
            mapParam.put("total_fee", payParams.get("money"));
            mapParam.put("spbill_create_ip", "192.168.200.1");
            mapParam.put("notify_url", notifyUrl);
            mapParam.put("trade_type", "NATIVE");
            // 包装附加参数
            Map<String, String> attach = new HashMap<>();
            attach.put("exchange", payParams.get("exchange"));
            attach.put("routingKey", payParams.get("routingKey"));
            // 若用户名不为空则保存一下用户名
            if (!StringUtils.isEmpty(payParams.get("username"))) {
                attach.put("username", payParams.get("username"));
            }
            mapParam.put("attach", JSONObject.toJSONString(attach));
            // 将map的数据转换为xml同时生成签名
            String xmlParams = WXPayUtil.generateSignedXml(mapParam, partnerkey);
            // 向这个地址发起POST请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParams);
            // 发起POST：请求就发出去
            httpClient.post();
            // 获取结果
            String contentXml = httpClient.getContent();
            // 解析结果：微信返回给我们的是XML数据
            return WXPayUtil.xmlToMap(contentXml);
        } catch (Exception e) {
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
    public Map<String, String> getPayResult(String orderId) {
        // 参数校验
        if (StringUtils.isEmpty(orderId)) {
            return null;
        }
        try {
            // 定义微信订单查询接口的URL
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            // 包装请求参数：XML数据
            Map<String, String> mapParam = new HashMap<>();
            mapParam.put("appid", appId);
            mapParam.put("mch_id", partner);
            mapParam.put("nonce_str", WXPayUtil.generateNonceStr());
            mapParam.put("out_trade_no", "java0509000000" + orderId);
            // 将map的数据转换为xml同时生成签名
            String xmlParams = WXPayUtil.generateSignedXml(mapParam, partnerkey);
            // 向这个地址发起POST请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParams);
            // 发起POST：请求就发出去
            httpClient.post();
            // 获取结果
            String contentXml = httpClient.getContent();
            // 解析结果：微信返回给我们的是XML数据
            return WXPayUtil.xmlToMap(contentXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭交易
     *
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> closePayUrl(String orderId) {
        // 参数校验
        if (StringUtils.isEmpty(orderId)) {
            return null;
        }
        try {
            // 定义微信订单查询接口的URL
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";
            // 包装请求参数：XML数据
            Map<String, String> mapParam = new HashMap<>();
            mapParam.put("appid", appId);
            mapParam.put("mch_id", partner);
            mapParam.put("nonce_str", WXPayUtil.generateNonceStr());
            mapParam.put("out_trade_no", "java0509000000" + orderId);
            // 将map的数据转换为xml同时生成签名
            String xmlParams = WXPayUtil.generateSignedXml(mapParam, partnerkey);
            // 向这个地址发起POST请求
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParams);
            // 发起POST：请求就发出去
            httpClient.post();
            // 获取结果
            String contentXml = httpClient.getContent();
            // 解析结果：微信返回给我们的是XML数据
            return WXPayUtil.xmlToMap(contentXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
