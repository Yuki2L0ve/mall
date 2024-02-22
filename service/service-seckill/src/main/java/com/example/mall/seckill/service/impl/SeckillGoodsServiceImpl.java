package com.example.mall.seckill.service.impl;

import com.example.mall.model.activity.SeckillGoods;
import com.example.mall.seckill.mapper.SeckillGoodsMapper;
import com.example.mall.seckill.service.SeckillGoodsService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 秒杀商品的接口实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Log4j2
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询指定时间段的商品列表
     *
     * @param time
     * @return
     */
    @Override
    public List<SeckillGoods> getSeckillGoods(String time) {
        return redisTemplate.opsForHash().values(time);
    }

    /**
     * 查询具体的秒杀商品数据
     *
     * @param time
     * @param goodsId
     * @return
     */
    @Override
    public SeckillGoods getSeckillGood(String time, String goodsId) {
        return (SeckillGoods) redisTemplate.opsForHash().get(time, goodsId);
    }

    /**
     * 同步指定时间段的商品剩余库存到数据库中去
     *
     * @param time
     */
    @Override
    public void updateSeckillGoodsStock(String time) {
        //从redis中获取该时间段的全部商品的id数据
        Set<String> keys = redisTemplate.opsForHash().keys("Seckill_Goods_Increment_" + time);
        if(keys != null && keys.size() > 0){
            //遍历同步每个商品的剩余库存数据到数据库中去
            keys.stream().forEach(goodsId ->{
                //获取redis中剩余库存
                Integer stockCount =
                        (Integer)redisTemplate.opsForHash().get("Seckill_Goods_Increment_" + time, goodsId);
                //同步到数据库
                int i = seckillGoodsMapper.updateSeckillGoodsStock(Long.valueOf(goodsId), stockCount);
                if(i < 0){
                    //1.使用定时任务扫描失败同步的日志,再次进行同步直到成功为止,但是若15次或20次都同步失败,2.发送短信/邮件通知人工处理
                    log.error("商品库存同步失败,所属的时间段为:" + time + ",失败的商品的id为:" + goodsId + ", 应该同步的剩余库存为:" + stockCount);
                }
                //哪个同步成功删除哪个
                redisTemplate.opsForHash().delete(goodsId);
            });
        }
    }
}
