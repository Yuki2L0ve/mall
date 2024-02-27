package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * 用户收货地址相关的接口类
 */
public interface UserAddressService {
    /**
     * 查询用户收货地址
     * @return
     */
    public List<UserAddress> getUserAddress();
}
