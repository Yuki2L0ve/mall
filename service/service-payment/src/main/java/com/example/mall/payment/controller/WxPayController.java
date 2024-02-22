package com.example.mall.payment.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.mall.payment.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付的控制层
 */
@RestController
@RequestMapping("/wx/pay")
public class WxPayController {
    @Resource
    private WxPayService wxPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 获取微信支付的二维码地址
     * @param payParams
     * @return
     */
    @GetMapping("/getPayCodeUrl")
    public Map<String, String> getPayCodeUrl(@RequestParam Map<String, String> payParams) {
        return wxPayService.getPayCode(payParams);
    }

    /**
     *  获取订单的支付结果
     * @param orderId
     * @return
     */
    @GetMapping("/getPayResult")
    public Map<String, String> getPayResult(@RequestParam String orderId) {
        return wxPayService.getPayResult(orderId);
    }

    /**
     *  关闭交易
     * @param orderId
     * @return
     */
    @GetMapping("/closePayUrl")
    public Map<String, String> closePayUrl(@RequestParam String orderId) {
        return wxPayService.closePayUrl(orderId);
    }

    /**
     * 给微信支付调用的通知地址，获取订单的支付结果
     * @param request
     * @return
     */
    @RequestMapping("/notify/callback")
    public String notifyCallback(HttpServletRequest request) {
        try {
//            // 获取微信传递过来的数据流
//            ServletInputStream is = request.getInputStream();
//            // 定义输出流
//            ByteArrayOutputStream os = new ByteArrayOutputStream();
//            // 定义缓冲区
//            byte[] buffer = new byte[1024];
//            // 定义读取的长度
//            int len = 0;
//            // 读取数据
//            while ((len = is.read(buffer)) != -1) {
//                os.write(buffer, 0, len);
//            }
//            os.flush();
//            // 流中的字节码提取出来
//            String xmlResult = new String(os.toByteArray());
//            // 将xml转换为map
//            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
//            // 获取附加参数
//            String attachJsonString = mapResult.get("attach");
//            // 反序列化
//            Map<String, String> attach = JSONObject.parseObject(attachJsonString, Map.class);
//            System.out.println(JSONObject.toJSONString(mapResult));
            String result = "{\"transaction_id\":\"4200001929202307190549847727\",\"nonce_str\":\"f1f2850f7dec4b95af80c99bd566b4f9\",\"bank_type\":\"OTHERS\",\"openid\":\"oHwsHuM8TuzLB3YVL6NMKh9hsB6o\",\"sign\":\"22D38AC94C9A69B0468D63795D3DA62F\",\"fee_type\":\"CNY\",\"mch_id\":\"1558950191\",\"cash_fee\":\"1\",\"out_trade_no\":\"1ebe43d92fdd46dea4db022dfebd672b\",\"appid\":\"wx74862e0dfcf69954\",\"total_fee\":\"1\",\"trade_type\":\"NATIVE\",\"result_code\":\"SUCCESS\",\"attach\":\"{\\\"exchange\\\":\\\"pay_exchange\\\",\\\"routingKey\\\":\\\"seckill.pay.order\\\",\\\"username\\\":\\\"lipei\\\"}\",\"time_end\":\"20230719211751\",\"is_subscribe\":\"N\",\"return_code\":\"SUCCESS\"}";
            Map<String, String> map = JSONObject.parseObject(result, Map.class);
            String attachJsonString = map.get("attach");
            Map<String, String> attach = JSONObject.parseObject(attachJsonString, Map.class);

            // 把这个结果告诉给订单微服务   不需要feign调用，发消息即可
            rabbitTemplate.convertAndSend(attach.get("exchange"),
                    attach.get("routingKey"),
                    result);
//            // 关闭流
//            is.close();
//            os.close();
            // 返回给微信结果
            Map<String, String> returnMap = new HashMap<>();
            returnMap.put("return_code", "SUCCESS");
            returnMap.put("return_msg", "OK");
            // 返回
            return WXPayUtil.mapToXml(returnMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
