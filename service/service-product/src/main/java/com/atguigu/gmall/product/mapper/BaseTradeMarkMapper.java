package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 品牌表的mapper映射
 */
@Mapper
public interface BaseTradeMarkMapper extends BaseMapper<BaseTrademark> {
}
