package com.example.mall.list.service.impl;

import com.example.mall.list.dao.GoodsDao;
import com.example.mall.list.service.GoodsService;
import com.example.mall.model.list.Goods;
import com.example.mall.model.list.SearchAttr;
import com.example.mall.model.product.BaseAttrInfo;
import com.example.mall.model.product.BaseCategoryView;
import com.example.mall.model.product.BaseTrademark;
import com.example.mall.model.product.SkuInfo;
import com.example.mall.product.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * es商品使用的接口实现类
 */
@Service
public class GoodsServiceImpl implements GoodsService {
    @Resource
    private GoodsDao goodsDao;
    @Autowired
    private ProductFeign productFeign;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 将商品从数据库中写入es中
     *
     * @param skuId
     */
    @Override
    public void dbSkuAddIntoEs(Long skuId) {
        // 查询商品的信息
        SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
        // 判断商品是否存在
        if (skuInfo == null || skuInfo.getId() == null)    return ;
        // 若商品存在，则将skuInfo对象转换为Goods对象
        Goods goods = new Goods();
        goods.setId(skuInfo.getId());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setTitle(skuInfo.getSkuName());
        // 查询实时价格
        BigDecimal price = productFeign.getPrice(skuId);
        goods.setPrice(price.doubleValue());
        goods.setCreateTime(new Date());
        // 品牌
        BaseTrademark baseTrademark = productFeign.getBaseTrademark(skuInfo.getTmId());
        goods.setTmId(baseTrademark.getId());
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        // 分类
        BaseCategoryView category = productFeign.getCategory(skuInfo.getCategory3Id());
        goods.setCategory1Id(category.getCategory1Id());
        goods.setCategory1Name(category.getCategory1Name());
        goods.setCategory2Id(category.getCategory2Id());
        goods.setCategory2Name(category.getCategory2Name());
        goods.setCategory3Id(category.getCategory3Id());
        goods.setCategory3Name(category.getCategory3Name());
        // 平台属性
        List<BaseAttrInfo> baseAttrInfoList = productFeign.getBaseAttrInfo(skuId);
        List<SearchAttr> attrs = baseAttrInfoList.stream().map(baseAttrInfo -> {
            // 初始化
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            // 返回
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(attrs);
        // 将数据保存到es中
        goodsDao.save(goods);
    }

    /**
     * 将商品从es中移除
     *
     * @param skuId
     */
    @Override
    public void removeGoodsFromEs(Long skuId) {
        // 直接将商品从es中删除
        goodsDao.deleteById(skuId);
    }

    /**
     * 增加商品的热度值
     *
     * @param goodsId
     */
    @Override
    public void addScore(Long goodsId) {
        // 参数校验
        if (goodsId == null)    return ;
        // 查询商品的数据
        Optional<Goods> optionalGoods = goodsDao.findById(goodsId);
        if (!optionalGoods.isPresent()) {
            // 使用zset进行热度值增加
            Double score = redisTemplate.opsForZSet().incrementScore("Goods_Hot_Score", goodsId + "", 1);
            // 每500更新一次
            if (score.intValue() % 500 == 0) {
                Goods goods = optionalGoods.get();
                goods.setHotScore(score.longValue());
                // 保存
                goodsDao.save(goods);
            }
        }
    }
}
