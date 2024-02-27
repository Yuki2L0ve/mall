package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 后台管理页面使用的服务层
 */
public interface ManageService {

    /**
     * 查询所有的一级分类
     * @return
     */
    public List<BaseCategory1> getBaseCategory1();

    /**
     * 根据一级分类id，查询出该一级分类下的所有二级分类
     * @param c1Id
     * @return
     */
    public List<BaseCategory2> getBaseCategory2(Long c1Id);

    /**
     * 根据二级分类id，查询出该二级分类下的所有三级分类
     * @param c2Id
     * @return
     */
    public List<BaseCategory3> getBaseCategory3(Long c2Id);

    /**
     * 保存平台属性
     * @param baseAttrInfo
     */
    public void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 获取平台属性列表
     * @param category3Id
     * @return
     */
    public List<BaseAttrInfo> getBaseAttrInfo(Long category3Id);

    /**
     * 删除平台属性
     * @param attrId
     */
    public void deleteBaseAttrInfo(Long attrId);

    /**
     * 查询所有的品牌列表
     * @return
     */
    public List<BaseTrademark> getBaseTrademark();

    /**
     * 查询所有的基础销售属性
     * @return
     */
    public List<BaseSaleAttr> getBaseSaleAttr();

    /**
     * 保存spu的信息
     * @param spuInfo
     */
    public void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 分页条件查询spuInfo
     *
     * @param page
     * @param size
     * @param category3Id
     * @return
     */
    public IPage<SpuInfo> pageSpuInfo(Integer page, Integer size, Long category3Id);

    /**
     * 根据SPU的id查询销售属性列表
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttr(Long spuId);

    /**
     * 查询指定SPU的图片列表
     * @param spuId
     * @return
     */
    public List<SpuImage> getSpuImage(Long spuId);

    /**
     * 保存SKU的信息
     * @param skuInfo
     */
    public void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 分页查询SKU的信息
     *
     * @param page
     * @param size
     * @return
     */
    public IPage<SkuInfo> list(Integer page, Integer size);

    /**
     * 商品上架或者下架
     * @param skuId
     * @param status
     */
    public void upOrDown(Long skuId, Short status);
}
