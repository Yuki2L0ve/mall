package com.example.mall.item.service;

import java.util.Map;

/**
 * 商品详情页面的接口
 */
public interface ItemService {

    /**
     * 根据商品id查询商品详情页需要的全部数据
     * @param skuId
     * @return
     */
    public Map<String, Object> getItemInfo(Long skuId);
}
