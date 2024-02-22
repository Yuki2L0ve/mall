package com.example.mall.seckill.service;

import com.example.mall.seckill.pojo.UserRecode;

/**
 * 秒杀下单使用的接口
 */
public interface SeckillOrderService {

    /**
     * 秒杀下单: 真实排队
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    public UserRecode addSeckillOrder(String time, String goodsId, Integer num);

    /**
     * 查询用户的排队状态
     * @return
     */
    public UserRecode getUserRecode();

    /**
     * 秒杀真实下单方法
     * @param userRecodeString
     */
    public void listenerAddSeckillOrder(String userRecodeString);

    /**
     * 取消秒杀订单: 超时取消和主动取消
     * @param username
     */
    public void cancelSeckillOrder(String username);

    /**
     * 修改支付结果
     * @param payResultJsonString
     */
    public void updateSeckillOrder(String payResultJsonString);
}
