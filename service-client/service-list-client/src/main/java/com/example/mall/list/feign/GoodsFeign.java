package com.example.mall.list.feign;

import com.example.mall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品搜索微服务提供的商品操作的feign接口
 */
@FeignClient(name = "service-list", path = "/api/list", contextId = "goodsFeign")
public interface GoodsFeign {
    /**
     * 商品同步到es中
     * @param skuId
     * @return
     */
    @GetMapping("/addGoods/{skuId}")
    public Result addGoods(@PathVariable("skuId") Long skuId);

    /**
     * 把商品从es中移除
     * @param goodsId
     * @return
     */
    @GetMapping("/removeGoods/{goodsId}")
    public Result removeGoods(@PathVariable("goodsId") Long goodsId);
}
