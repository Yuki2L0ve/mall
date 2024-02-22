package com.example.mall.list.controller;

import com.example.mall.common.result.Result;
import com.example.mall.list.service.GoodsService;
import com.example.mall.model.list.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品相关的控制层
 */
@RestController
@RequestMapping("/api/list")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 创建索引创建映射
     * @return
     */
    @GetMapping("/createIndexAndMapping")
    public Result createIndexAndMapping() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    /**
     * 商品同步到es中
     * @param skuId
     * @return
     */
    @GetMapping("/addGoods/{skuId}")
    public Result addGoods(@PathVariable("skuId") Long skuId) {
        goodsService.dbSkuAddIntoEs(skuId);
        return Result.ok();
    }

    /**
     * 把商品从es中移除
     * @param goodsId
     * @return
     */
    @GetMapping("/removeGoods/{goodsId}")
    public Result removeGoods(@PathVariable("goodsId") Long goodsId) {
        goodsService.removeGoodsFromEs(goodsId);
        return Result.ok();
    }

    /**
     * 增加商品的热度值
     * @param goodsId
     * @return
     */
    @GetMapping("/addScore")
    public Result addScore(Long goodsId) {
        goodsService.addScore(goodsId);
        return Result.ok();
    }
}
