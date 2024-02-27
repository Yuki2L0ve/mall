package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.GoodsService;
import com.atguigu.gmall.model.list.Goods;
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
     * 创建索引和映射
     * @return
     */
    @GetMapping("/createIndexAndMapping")
    public Result createIndexAndMapping() {
        // 这个方法的作用是在Elasticsearch中创建一个与Goods类对应的索引。
        // 如果索引已经存在，这个方法会检查索引的映射是否与Goods类的映射一致，如果不一致，它会更新映射。
        elasticsearchRestTemplate.createIndex(Goods.class);
        // 这个方法用于在Elasticsearch中为Goods类定义或更新映射。映射定义了Goods类属性与Elasticsearch字段之间的对应关系，以及如何索引这些字段。
        // 如果映射已经存在，这个方法会更新映射以匹配Goods类的当前状态。
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
