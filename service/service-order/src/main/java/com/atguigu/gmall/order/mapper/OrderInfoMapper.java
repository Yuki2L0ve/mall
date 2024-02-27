package com.atguigu.gmall.order.mapper;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 订单表的mapper映射
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Update("update order_infoset order_status = #{finalStatus}, set process_status = #{finalStatus} where id = #{orderId} and order_status = #{orderStatus}")
    public int cancelOrder(Long orderId, String orderStatus, String finalStatus);
}
