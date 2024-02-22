package com.example.mall.order.mapper;


import com.example.mall.model.payment.PaymentInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付信息表的mapper映射
 */
@Mapper
public interface PaymentInfoMapper extends BaseMapper<PaymentInfo> {
}
