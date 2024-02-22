package com.example.mall.seckill.mapper;

import com.example.mall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 秒杀商品的mapper映射
 */
@Mapper
public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {
    /**
     * 同步库存
     * @param goodsId
     * @param stockCount
     * @return
     */
    @Update("update seckill_goods set stock_count = #{stockCount} where id = #{goodsId}")
    public int updateSeckillGoodsStock(Long goodsId, Integer stockCount);
}
