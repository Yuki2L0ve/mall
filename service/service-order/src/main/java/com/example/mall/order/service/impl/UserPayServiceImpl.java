package com.example.mall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.mall.common.constant.OrderConst;
import com.example.mall.model.enums.OrderStatus;
import com.example.mall.model.order.OrderInfo;
import com.example.mall.model.payment.PaymentInfo;
import com.example.mall.order.mapper.OrderInfoMapper;
import com.example.mall.order.mapper.PaymentInfoMapper;
import com.example.mall.order.service.UserPayService;
import com.example.mall.order.util.OrderThreadLocalUtil;
import com.example.mall.payment.feign.PaymentWxFeign;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 记录用户的支付渠道和信息的接口实现类
 */
@Service
public class UserPayServiceImpl implements UserPayService {
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private PaymentWxFeign paymentWxFeign;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * @param orderId
     * @param payway  微信 或 支付宝
     * @return
     */
    @Override
    public String getOrderPayUrl(Long orderId, String payway) {
        // 参数校验
        if (orderId == null) {
            throw new RuntimeException("无法获取支付信息，订单号不能为空！！");
        }
        // 先从redis中看这个订单有没有支付地址
        PaymentInfo paymentInfo = (PaymentInfo) redisTemplate.opsForValue().get("Wait_Pay_OrderInfo_" + orderId);
        if (paymentInfo != null && paymentInfo.getId() != null) {
            // 假设已经可以判断是否更换了渠道，没有更换渠道则直接返回支付地址
            if (paymentInfo.getPaymentType().equals(payway)) {
                // 没有更换支付渠道, 直接返回上一次申请的渠道信息
                return paymentInfo.getPayUrl();
            } else {
                // 更换了支付渠道，5分钟内不允许更换渠道
                long times = System.currentTimeMillis() - paymentInfo.getCreateTime().getTime();
                if (times < 300000) {
                    // 还在5分钟内，那么不能更换支付渠道，仍然返回之前的渠道信息
                    return paymentInfo.getPayUrl();
                } else {
                    // 5分钟及以上的，关闭渠道---远程调用支付微服务，关闭交易
                    Map<String, String> closeResult = paymentWxFeign.closePayUrl(orderId + "");
                    if (!closeResult.get("return_code").equals("SUCCESS") ||
                        !closeResult.get("result_code").equals("SUCCESS")) {
                        // 关闭交易失败
                        return paymentInfo.getPayUrl();
                    }
                }
            }
        }

        // 使用分布式锁防止多次申请支付地址，一笔订单只允许有一个支付渠道和地址
        RLock lock = redissonClient.getLock("Order_Pay_Count_" + orderId);
        if (lock.tryLock()) {
            try {
                // 查询订单的信息
                OrderInfo orderInfo = orderInfoMapper.selectOne(
                        new LambdaQueryWrapper<OrderInfo>()
                                .eq(OrderInfo::getId, orderId)
                                .eq(OrderInfo::getUserId, OrderThreadLocalUtil.get())
                                .eq(OrderInfo::getOrderStatus, OrderStatus.UNPAID.getComment()));
                if (orderInfo == null || orderInfo.getId() == null) {
                    throw new RuntimeException("用户没有待支付的订单信息，获取支付信息失败！！！");
                }
                // 获取支付需要的必要金额
                BigDecimal totalAmount = orderInfo.getTotalAmount();
                String desc = "尚硅谷商城的订单";
                // 判断是否为第一次，若为第一次则需要进行初始化
                if (paymentInfo == null) {
                    paymentInfo = new PaymentInfo();
                }
                // 用户存在待支付的订单信息
                if (payway.equals(OrderConst.WX_PAY)) {// 微信渠道
                    // 将单位转换为分
                    totalAmount = orderInfo.getTotalAmount().multiply(new BigDecimal(100));
                    // 远程调用支付微服务，获取微信支付的二维码地址
                    Map<String, String> wxPayMap =
                            paymentWxFeign.getPayCodeUrl(orderId + "", totalAmount.intValue() + "", desc);
                    // 测试代码：使用1分钱进行测试
                    //paymentWxFeign.getPayCodeUrl(orderId + "", "1", desc);
                    if (wxPayMap.get("return_code").equals("SUCCESS") &&
                            wxPayMap.get("result_code").equals("SUCCESS")) {
                        // 获取支付二维码地址成功
                        paymentInfo.setPayUrl(wxPayMap.get("code_url"));
                        // 保存wx渠道
                        paymentInfo.setPaymentType(OrderConst.WX_PAY);
                    }
                } else {
                    // 支付宝渠道 --TODO
                    paymentInfo.setPaymentType(OrderConst.ZFB_PAY);
                }
                // 重新计时
                paymentInfo.setCreateTime(new Date());
                // 判断第一次还是更换
                if (paymentInfo.getId() == null) {
                    // 第一次申请地址
                    paymentInfo.setOrderId(orderId);
                    paymentInfo.setTotalAmount(totalAmount);
                    paymentInfo.setSubject(desc);
                    paymentInfo.setPaymentStatus("待支付");
                    int insert = paymentInfoMapper.insert(paymentInfo);
                    if (insert <= 0) {
                        throw new RuntimeException("保存支付信息失败，获取支付地址失败！！");
                    }
                } else {
                    // 更换地址
                    int update = paymentInfoMapper.updateById(paymentInfo);
                    if (update < 0) {
                        throw new RuntimeException("保存支付信息失败，获取支付地址失败！！");
                    }
                }
                // 写入redis，防止用户频繁地查询
                redisTemplate.opsForValue().set("Wait_Pay_OrderInfo_" + orderId, paymentInfo);
                // 返回支付信息
                return paymentInfo.getPayUrl();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 释放锁
                lock.unlock();
            }
        } else {
            // 这笔订单正在申请支付地址
            throw new RuntimeException("订单正在进行支付，不要重复申请支付地址！！！");
        }

        return null;
    }
}
