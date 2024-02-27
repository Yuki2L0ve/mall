package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 供远程调用查询商品数据的内部接口的实现类
 */
@Service
@Log4j2
public class ItemServiceImpl implements ItemService {
    @Resource
    private SkuInfoMapper skuInfoMapper;
    @Resource
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Resource
    private SkuImageMapper skuImageMapper;
    @Resource
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Resource
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Resource
    private BaseTradeMarkMapper baseTradeMarkMapper;
    @Resource
    private BaseAttrInfoMapper baseAttrInfoMapper;

    /**
     * 根据商品id查询商品的详情
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return skuInfoMapper.selectById(skuId);
    }

    /**
     * 根据商品id查询商品的详情 从redis或者数据库中查
     *
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfoFromRedisOrMysql(Long skuId) {
        // 查询redis中是否存在商品数据  --> 数据的 key = sku:1:info
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get("sku:" + skuId + ":info");
        // 如果redis中存在该商品，则返回
        if (skuInfo != null)    return skuInfo;
        // 如果redis中没有商品数据，则获取锁  --> 锁的 key = sku:1:lock
        RLock lock = redissonClient.getLock("sku:" + skuId + ":lock");
        try {
            // 尝试加锁
            if (lock.tryLock(100, 100, TimeUnit.SECONDS)) {
                try {
                    // 加锁成功的线程去数据库里面查询数据
                    skuInfo = skuInfoMapper.selectById(skuId);
                    // 判断数据库中商品的数据是否存在：1.数据库中也没有    2.数据库中有
                    if (skuInfo == null || skuInfo.getId() == null) {
                        // 数据库中没有，此时redis和数据库中都没有，那么就在redis中缓存5小时的空值-->为了解决缓存穿透问题
                        skuInfo = new SkuInfo();
                        redisTemplate.opsForValue().set("sku:" + skuId + ":info", skuInfo, 60 * 60 * 5, TimeUnit.SECONDS);
                    } else {
                        // 数据库中有 那么则写入redis，商品的有效期为1天
                        redisTemplate.opsForValue().set("sku:" + skuId + ":info", skuInfo, 60 * 60 * 24, TimeUnit.SECONDS);
                    }
                    // 返回商品数据
                    return skuInfo;
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("加锁成功，但是处理查询商品数据库时出现逻辑错误！商品的id为：" + skuId);
                } finally {
                    // 释放锁
                    lock.unlock();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("商品加锁失败，商品的id为：" + skuId);
        }

        return null;
    }

    /**
     * 根据三级分类id查询出 一级、二级、三级分类的全部信息
     *
     * @param category3Id
     * @return
     */
    @Override
    public BaseCategoryView getCategory(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    /**
     * 查询商品的图片列表
     *
     * @param skuId
     * @return
     */
    @Override
    public List<SkuImage> getImageList(Long skuId) {
        return skuImageMapper.selectList(new LambdaQueryWrapper<SkuImage>().eq(SkuImage::getSkuId, skuId));
    }

    /**
     * 根据商品id查询商品的价格
     *
     * @param skuId
     * @return
     */
    @Override
    public BigDecimal getPrice(Long skuId) {
        return skuInfoMapper.selectById(skuId).getPrice();
    }

    /**
     * 查询指定的sku所属的spu的全部销售属性和值，并且标识出应该选中的值是哪几个
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttr(Long skuId, Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrBySpuIdAndSkuId(skuId, spuId);
    }

    /**
     * 查询页面跳转使用的键值对
     *
     * @param spuId
     * @return
     */
    @Override
    public Map getSkuIdAndSaleValues(Long spuId) {
        // 初始化返回结果
        /*
        注意：由于我们使用了流式处理进行循环，这就意味着多线程同时操作resultList，
        而HashMap是线程不安全的，ConcurrentHashMap是线程安全的.
        因此这里必须使用ConcurrentHashMap
         */
        Map<Object, Object> result = new ConcurrentHashMap<>();
        // 获取所有的skuId和销售属性值的内容
        List<Map> resultList = skuSaleAttrValueMapper.getSkuSaleAttrInfoBySpuId(spuId);
        // 遍历将值保存到一个map中
        resultList.stream().forEach(mp -> {
            Object skuId = mp.get("sku_id");
            Object idValues = mp.get("id_values");
            // 保存结果
            result.put(idValues, skuId);
        });

        return result;
    }

    /**
     * 查询品牌的详细信息
     *
     * @param id
     * @return
     */
    @Override
    public BaseTrademark getBaseTrademark(Long id) {
        return baseTradeMarkMapper.selectById(id);
    }

    /**
     * 查询指定商品的平台属性信息
     *
     * @param skuId
     * @return
     */
    @Override
    public List<BaseAttrInfo> getBaseAttrInfo(Long skuId) {
        return baseAttrInfoMapper.selectBaseAttrInfoBySkuId(skuId);
    }

    /**
     * 扣减库存
     *
     * @param decountMap    decountMap.put(cartInfo.getSkuId() + "", cartInfo.getSkuNum() + "");
     */
    @Override
    public void decountStock(Map<String, String> decountMap) {
        // 遍历扣减库存
        decountMap.entrySet().stream().forEach(entry -> {
            // 获取商品的id
            String key = entry.getKey();
            // 获取商品的扣减数量
            String value = entry.getValue();
            // 使用sql语句完成获取商品，判断库存，修改库存操作
            int i = skuInfoMapper.decountStock(Integer.parseInt(value), Long.parseLong(key));
            if (i <= 0) {
                throw new RuntimeException("商品库存扣减失败！！！");
            }

//            // 查询商品的数据
//            SkuInfo skuInfo = skuInfoMapper.selectById(Long.valueOf(key));
//            if (skuInfo == null || skuInfo.getId() == null) {
//                throw new RuntimeException("商品库存扣减失败，商品不存在！！！");
//            }
//            // 扣减库存
//            int stock = skuInfo.getStock() - Integer.parseInt(value);
//            if (stock < 0) {
//                throw new RuntimeException("商品库存扣减失败，商品库存不足！！！");
//            }
//            // 修改商品的库存
//            skuInfo.setStock(stock);
//            int update = skuInfoMapper.updateById(skuInfo);
//            if (update < 0) {
//                throw new RuntimeException("商品库存扣减失败！！！");
//            }
        });
    }

    /**
     * 回滚库存
     * @param rollbackMap
     */
    @Override
    public void rollbackStock(Map<String, String> rollbackMap) {
        // 遍历进行库存回滚
        rollbackMap.entrySet().stream().forEach(entry -> {
            // 商品id
            String key = entry.getKey();
            // 数量
            String value = entry.getValue();
            // 回滚
            int i = skuInfoMapper.rollbackStock(Integer.parseInt(value), Long.parseLong(key));
            if (i < 0) {
                throw new RuntimeException("回滚库存失败！！");
            }
        });
    }
}
