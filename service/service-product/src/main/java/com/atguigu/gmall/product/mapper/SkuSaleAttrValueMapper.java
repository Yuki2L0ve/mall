package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * SKU销售属性表的mapper映射
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    /**
     * 根据指定的spuId查询这个SPU所有拥有的全部SKU的id和每个SKU对应的销售属性值的内容
     * @param spuId
     * @return
     */
    @Select("SELECT sku_id,GROUP_CONCAT( DISTINCT sale_attr_value_id ORDER BY sale_attr_value_id SEPARATOR '|' ) AS id_values FROM\n" +
            "sku_sale_attr_value  WHERE spu_id = 31 GROUP BY sku_id")
    public List<Map> getSkuSaleAttrInfoBySpuId(@Param("spuId") Long spuId);
}
