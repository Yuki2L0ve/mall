package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.model.user.UserAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户收货地址的mapper映射
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
