package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * SKU表的mapper映射
 */
@Mapper
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    /**
     * 扣减库存
     * @param stock
     * @param skuId
     * @return
     */
    @Update("update sku_info set stock = stock - #{stock} where id = #{skuId} and stock >= #{stock}")
    public int decountStock(@Param("stock") Integer stock, @Param("skuId") Long skuId);

    /**
     * 回滚库存
     * @param stock
     * @param skuId
     * @return
     */
    @Update("update sku_info set stock = stock + #{stock} where id = #{skuId}")
    public int rollbackStock(@Param("stock") Integer stock, @Param("skuId") Long skuId);
}
