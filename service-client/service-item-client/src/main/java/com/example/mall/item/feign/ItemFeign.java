package com.example.mall.item.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情微服务提供的feign接口
 */
@FeignClient(name = "service-item", path = "/item/info", contextId = "itemFeign")
public interface ItemFeign {
    /**
     * 获取商品详情页需要的全部数据
     * @param skuId
     * @return
     */
    @GetMapping("/getItemInfo/{skuId}")
    public Map<String, Object> getItemInfo(@PathVariable Long skuId);
}
