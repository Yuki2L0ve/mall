package com.example.mall.list.service;

/**
 * es商品使用的接口类
 */
public interface GoodsService {

    /**
     * 将商品从数据库中写入es中
     * @param skuId
     */
    public void dbSkuAddIntoEs(Long skuId);

    /**
     * 将商品从es中移除
     * @param skuId
     */
    public void removeGoodsFromEs(Long skuId);

    /**
     * 增加商品的热度值
     * @param goodsId
     */
    public void addScore(Long goodsId);
}
