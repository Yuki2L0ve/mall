package com.example.mall.product.service;

import com.example.mall.model.product.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 供远程调用查询商品数据的内部接口类
 */
public interface ItemService {

    /**
     * 根据商品id查询商品的详情
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfo(Long skuId);

    /**
     * 根据商品id查询商品的详情 从redis或者数据库中查
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoFromRedisOrMysql(Long skuId);

    /**
     * 根据三级分类id查询出 一级、二级、三级分类的全部信息
     * @param category3Id
     * @return
     */
    public BaseCategoryView getCategory(Long category3Id);

    /**
     * 查询商品的图片列表
     * @param skuId
     * @return
     */
    public List<SkuImage> getImageList(Long skuId);

    /**
     * 根据商品id查询商品的价格
     * @param skuId
     * @return
     */
    public BigDecimal getPrice(Long skuId);

    /**
     * 查询指定的sku所属的spu的全部销售属性和值，并且标识出应该选中的值是哪几个
     * @param skuId
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttr(Long skuId, Long spuId);

    /**
     * 查询页面跳转使用的键值对
     * @param spuId
     * @return
     */
    public Map getSkuIdAndSaleValues(Long spuId);

    /**
     * 查询品牌的详细信息
     * @param id
     * @return
     */
    public BaseTrademark getBaseTrademark(Long id);

    /**
     * 查询指定商品的平台属性信息
     * @param skuId
     * @return
     */
    public List<BaseAttrInfo> getBaseAttrInfo(Long skuId);

    /**
     * 扣减库存
     * @param decountMap
     */
    public void decountStock(Map<String, String> decountMap);

    /**
     * 回滚库存
     * @param rollbackMap
     */
    public void rollbackStock(Map<String, String> rollbackMap);
}
