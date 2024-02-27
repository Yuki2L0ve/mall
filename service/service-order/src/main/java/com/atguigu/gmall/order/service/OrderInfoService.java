package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

/**
 * 订单相关的接口类
 */
public interface OrderInfoService {
    /**
     * 新增订单
     * @param orderInfo
     */
    public void addOrder(OrderInfo orderInfo);

    /**
     * 取消订单： 1.主动  2.超时
     * @param orderId
     */
    public void cancelOrder(Long orderId);

    /**
     * 修改订单的支付结果
     * @param resultJsonString
     */
    public void updateOrder(String resultJsonString);
}
