package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台属性的mapper映射
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    /**
     * 根据分类查询平台属性列表
     * @param categoryId
     * @return
     */
    public List<BaseAttrInfo> selectBaseAttrInfoByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询指定sku的平台属性名称和值（一个平台属性名称对一个平台属性值）
     * @param skuId
     * @return
     */
    public List<BaseAttrInfo> selectBaseAttrInfoBySkuId(@Param("skuId") Long skuId);
}
