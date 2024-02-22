package com.example.mall.item.controller;

import com.example.mall.common.result.Result;
import com.example.mall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品详情页面的控制层
 */
@RestController
@RequestMapping("/item/info")
public class ItemController {
    @Autowired
    private ItemService itemService;

    /**
     * 获取商品详情页需要的全部数据
     * @param skuId
     * @return
     */
    @GetMapping("/getItemInfo/{skuId}")
    public Map<String, Object> getItemInfo(@PathVariable Long skuId) {
        return itemService.getItemInfo(skuId);
    }
}
