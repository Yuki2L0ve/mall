package com.example.mall.product.feign;

import com.example.mall.model.product.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 查询商品详情使用的feign接口模块
 */
@FeignClient(name = "service-product", path = "/api/item", contextId = "productFeign")
public interface ProductFeign {
    /**
     * 根据商品id查询商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId);

    /**
     * 根据三级分类id查询出 一级、二级、三级分类的全部信息
     * 查询详情页需要的分类信息
     * @param category3Id
     * @return
     */
    @GetMapping("/getCategory/{category3Id}")
    public BaseCategoryView getCategory(@PathVariable Long category3Id);

    /**
     * 查询商品的图片列表
     * @param skuId
     * @return
     */
    @GetMapping("/getImageList/{skuId}")
    public List<SkuImage> getImageList(@PathVariable Long skuId);

    /**
     * 根据商品id查询商品的价格
     * @param skuId
     * @return
     */
    @GetMapping("/getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable Long skuId);

    /**
     * 查询指定的sku所属的spu的全部销售属性和值，并且标识出应该选中的值是哪几个
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/getSpuSaleAttr/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttr(@PathVariable Long skuId, @PathVariable Long spuId);

    /**
     * 查询页面跳转使用的键值对
     * @param spuId
     * @return
     */
    @GetMapping("/getSkuIdAndSaleValues/{spuId}")
    public Map getSkuIdAndSaleValues(@PathVariable Long spuId);

    /**
     * 查询品牌的信息
     * @param id
     * @return
     */
    @GetMapping("/getBaseTrademark/{id}")
    public BaseTrademark getBaseTrademark(@PathVariable("id") Long id);

    /**
     * 查询指定sku的平台属性信息
     * @param skuId
     * @return
     */
    @GetMapping("/getBaseAttrInfo/{skuId}")
    public List<BaseAttrInfo> getBaseAttrInfo(@PathVariable("skuId") Long skuId);

    /**
     * 扣减库存
     * @param decountMap
     */
    @GetMapping("/decountStock")
    public void decountStock(@RequestParam Map<String, String> decountMap);

    /**
     * 回滚库存
     * @param rollbackMap
     */
    @GetMapping("/rollbackStock")
    public void rollbackStock(@RequestParam Map<String, String> rollbackMap);
}
