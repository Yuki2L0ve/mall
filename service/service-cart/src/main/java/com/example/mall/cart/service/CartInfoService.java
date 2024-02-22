package com.example.mall.cart.service;

import com.example.mall.model.cart.CartInfo;

import java.util.List;
import java.util.Map;

/**
 * 购物车相关的接口类
 */
public interface CartInfoService {
    /**
     * 新增购物车
     * @param skuId
     * @param num
     */
    public void addCart(Long skuId, Integer num);

    /**
     * 查询指定用户的购物车数据
     * @return
     */
    public List<CartInfo> getCartInfo();

    /**
     * 删除购物车
     * @param id
     */
    public void removeCart(Long id);

    /**
     * 修改购物车
     * @param id
     * @param num
     */
    public void updateCartNum(Long id, Integer num);

    /**
     * 修改选中状态
     * @param id
     * @param status
     */
    public void checkUpdate(Long id, Short status);

    /**
     * 登录后调用：合并用户在未登录场景下添加的购物车数据
     * @param cartInfoList
     */
    public void mergeCart(List<CartInfo> cartInfoList);

    /**
     * 查询用户本次购买的购物车数据
     *
     * @return
     */
    public Map<String, Object> getOrderConfirmCart();

    /**
     * 删除购物车
     */
    public void deleteCart();
}
