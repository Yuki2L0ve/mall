package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SpuSaleAttr销售属性名称表的mapper映射
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {

    /**
     * 根据SPU的id查询销售属性列表
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuAttrBySpuId(@Param("spuId") Long spuId);

    /**
     * 查询指定的sku所属的spu的全部销售属性和值，并且标识出应该选中的值是哪几个
     * @param skuId
     * @param spuId
     * @return
     */
    public List<SpuSaleAttr> selectSpuSaleAttrBySpuIdAndSkuId(@Param("skuId") Long skuId, @Param("spuId") Long spuId);
}
