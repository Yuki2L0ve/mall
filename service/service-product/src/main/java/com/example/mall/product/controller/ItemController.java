package com.example.mall.product.controller;

import com.example.mall.common.cache.Java0509Cache;
import com.example.mall.common.result.Result;
import com.example.mall.model.product.*;
import com.example.mall.product.service.ItemService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 供远程调用查询商品数据的内部接口的控制层
 */
@RestController
@RequestMapping("/api/item")
public class ItemController {
    @Resource
    private ItemService itemService;

    /**
     * 根据商品id查询商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfo/{skuId}")
    @Java0509Cache(prefix = "getSkuInfo:")
    public SkuInfo getSkuInfo(@PathVariable Long skuId) {
        return itemService.getSkuInfo(skuId);
        //return itemService.getSkuInfoFromRedisOrMysql(skuId);
    }

    /**
     * 根据三级分类id查询出 一级、二级、三级分类的全部信息
     * 查询详情页需要的分类信息
     * @param category3Id
     * @return
     */
    @GetMapping("/getCategory/{category3Id}")
    @Java0509Cache(prefix = "getCategory:")
    public BaseCategoryView getCategory(@PathVariable Long category3Id) {
        return itemService.getCategory(category3Id);
    }

    /**
     * 查询商品的图片列表
     * @param skuId
     * @return
     */
    @GetMapping("/getImageList/{skuId}")
    @Java0509Cache(prefix = "getImageList:")
    public List<SkuImage> getImageList(@PathVariable Long skuId) {
        return itemService.getImageList(skuId);
    }

    /**
     * 根据商品id查询商品的价格
     * @param skuId
     * @return
     */
    @GetMapping("/getPrice/{skuId}")
    @Java0509Cache(prefix = "getPrice:")
    public BigDecimal getPrice(@PathVariable Long skuId) {
        return itemService.getPrice(skuId);
    }

    /**
     * 查询指定的sku所属的spu的全部销售属性和值，并且标识出应该选中的值是哪几个
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/getSpuSaleAttr/{skuId}/{spuId}")
    @Java0509Cache(prefix = "getSpuSaleAttr:")
    public List<SpuSaleAttr> getSpuSaleAttr(@PathVariable Long skuId, @PathVariable Long spuId) {
        return itemService.getSpuSaleAttr(skuId, spuId);
    }

    /**
     * 查询页面跳转使用的键值对
     * @param spuId
     * @return
     */
    @GetMapping("/getSkuIdAndSaleValues/{spuId}")
    @Java0509Cache(prefix = "getSkuIdAndSaleValues:")
    public Map getSkuIdAndSaleValues(@PathVariable Long spuId) {
        return itemService.getSkuIdAndSaleValues(spuId);
    }

    /**
     * 查询品牌的信息
     * @param id
     * @return
     */
    @GetMapping("/getBaseTrademark/{id}")
    @Java0509Cache(prefix = "getBaseTrademark:")
    public BaseTrademark getBaseTrademark(@PathVariable("id") Long id) {
        return itemService.getBaseTrademark(id);
    }

    /**
     * 查询指定sku的平台属性信息
     * @param skuId
     * @return
     */
    @GetMapping("/getBaseAttrInfo/{skuId}")
    public List<BaseAttrInfo> getBaseAttrInfo(@PathVariable("skuId") Long skuId) {
        return itemService.getBaseAttrInfo(skuId);
    }

    /**
     * 扣减库存
     * @param decountMap
     */
    @GetMapping("/decountStock")
    public void decountStock(@RequestParam Map<String, String> decountMap) {
        itemService.decountStock(decountMap);
    }

    /**
     * 回滚库存
     * @param rollbackMap
     */
    @GetMapping("/rollbackStock")
    public void rollbackStock(@RequestParam Map<String, String> rollbackMap) {
        itemService.rollbackStock(rollbackMap);
    }
}
