package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

/**
 * 秒杀商品的接口
 */
public interface SeckillGoodsService {

    /**
     * 查询指定时间段的商品列表
     * @param time
     * @return
     */
    public List<SeckillGoods> getSeckillGoods(String time);

    /**
     * 查询具体的秒杀商品数据
     * @param time
     * @param goodsId
     * @return
     */
    public SeckillGoods getSeckillGood(String time, String goodsId);

    /**
     * 同步指定时间段的商品剩余库存到数据中
     * @param time
     */
    public void updateSeckillGoodsStock(String time);
}
