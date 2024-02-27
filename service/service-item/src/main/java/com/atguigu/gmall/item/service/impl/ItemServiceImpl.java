package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 商品详情页使用的接口实现类
 */
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private ProductFeign productFeign;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 根据商品id查询商品详情页需要的全部数据
     *
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getItemInfo(Long skuId) {
        // 返回结果初始化
        Map<String, Object> result = new ConcurrentHashMap<>();
        // 参数校验
        if (skuId == null)  return result;

//        // 查询商品数据
//        SkuInfo skuInfo = itemFeign.getSkuInfo(skuId);
//        // 判断商品是否存在
//        if (skuInfo == null || skuInfo.getId() == null )    return result;
//        // 保存商品的信息
//        result.put("skuInfo", skuInfo);

        // 查询商品数据
        CompletableFuture<SkuInfo> future1 = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
            // 判断商品是否存在  如果商品不存在，则结束
            if (skuInfo == null || skuInfo.getId() == null) return null;
            // 保存商品的信息
            result.put("skuInfo", skuInfo);
            // 返回任务结果
            return skuInfo;
        }, threadPoolExecutor);

//        // 查询分类信息
//        Long category3Id = skuInfo.getCategory3Id();
//        BaseCategoryView category = itemFeign.getCategory(category3Id);
//        result.put("category", category);

        // 查询分类信息
        CompletableFuture<Void> future2 = future1.thenAcceptAsync((skuInfo) -> {
            // 判断商品是否存在
            if (skuInfo == null) return;
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView category = productFeign.getCategory(category3Id);
            result.put("category", category);
        }, threadPoolExecutor);

//        // 查询图片列表
//        List<SkuImage> imageList = itemFeign.getImageList(skuId);
//        result.put("imageList", imageList);

        // 查询图片列表
        CompletableFuture<Void> future3 = future1.thenAcceptAsync((skuInfo) -> {
            // 判断商品是否存在
            if (skuInfo == null) return;
            List<SkuImage> imageList = productFeign.getImageList(skuInfo.getId());
            result.put("imageList", imageList);
        }, threadPoolExecutor);

//        // 查询商品的价格
//        BigDecimal price = itemFeign.getPrice(skuId);
//        result.put("price", price);

        // 查询商品价格
        CompletableFuture<Void> future4 = future1.thenAcceptAsync((skuInfo) -> {
            // 判断商品是否存在
            if (skuInfo == null) return;
            BigDecimal price = productFeign.getPrice(skuInfo.getId());
            result.put("price", price);
        }, threadPoolExecutor);

//        // 查询销售属性信息
//        List<SpuSaleAttr> spuSaleAttrList = itemFeign.getSpuSaleAttr(skuId, skuInfo.getSpuId());
//        result.put("spuSaleAttrList", spuSaleAttrList);

        // 查询销售属性信息
        CompletableFuture<Void> future5 = future1.thenAcceptAsync((skuInfo) -> {
            // 判断商品是否存在
            if (skuInfo == null) return;
            List<SpuSaleAttr> spuSaleAttrList = productFeign.getSpuSaleAttr(skuInfo.getId(), skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrList);
        }, threadPoolExecutor);

//        // 查询页面跳转用的键值对
//        Map jumpMap = itemFeign.getSkuIdAndSaleValues(skuInfo.getSpuId());
//        result.put("jump", jumpMap);

        // 查询页面跳转用的键值对
        CompletableFuture<Void> future6 = future1.thenAcceptAsync((skuInfo) -> {
            // 判断商品是否存在
            if (skuInfo == null) return;
            Map jumpMap = productFeign.getSkuIdAndSaleValues(skuInfo.getSpuId());
            result.put("jump", jumpMap);
        }, threadPoolExecutor);

        // 等待所有的任务执行完成后返回
        CompletableFuture.allOf(future1, future2, future3, future4, future5, future6).join();
        // 将以上的结果全部整合后，返回
        return result;
    }
}
