package com.example.mall.user.service.impl;

import com.example.mall.model.user.UserAddress;
import com.example.mall.user.mapper.UserAddressMapper;
import com.example.mall.user.service.UserAddressService;
import com.example.mall.user.util.UserThreadLocalUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户收货地址相关的接口实现类
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {
    @Resource
    private UserAddressMapper userAddressMapper;

    /**
     * 查询用户收货地址
     *
     * @return
     */
    @Override
    public List<UserAddress> getUserAddress() {
        return userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, UserThreadLocalUtil.get()));
    }
}
