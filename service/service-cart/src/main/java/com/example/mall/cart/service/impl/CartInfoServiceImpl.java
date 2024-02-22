package com.example.mall.cart.service.impl;

import com.example.mall.cart.mapper.CartInfoMapper;
import com.example.mall.cart.service.CartInfoService;
import com.example.mall.cart.util.CartThreadLocalUtil;
import com.example.mall.common.constant.CartConst;
import com.example.mall.model.base.BaseEntity;
import com.example.mall.model.cart.CartInfo;
import com.example.mall.model.product.SkuInfo;
import com.example.mall.product.feign.ProductFeign;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.util.concurrent.AtomicDouble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 购物车相关的接口实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CartInfoServiceImpl implements CartInfoService {
    @Resource
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private ProductFeign productFeign;

    /**
     * 新增购物车
     *
     * @param skuId
     * @param num
     */
    @Override
    public void addCart(Long skuId, Integer num) {
        // 参数校验
        if (skuId == null || num == null) {
            throw new RuntimeException("参数错误！");
        }
        // 查询商品是否存在
        SkuInfo skuInfo = productFeign.getSkuInfo(skuId);
        if (skuInfo == null || skuInfo.getId() == null) {
            throw new RuntimeException("商品不存在!!!");
        }
        // 判断用户购物车中是否已经有了这个商品
        CartInfo cartInfo = cartInfoMapper.selectOne(
                new LambdaQueryWrapper<CartInfo>().
                        eq(CartInfo::getSkuId, skuId).
                        eq(CartInfo::getUserId, CartThreadLocalUtil.get()));
        if (cartInfo == null) { // 新增
            if (num <= 0)   return ;
            // 构建购物车对象
            cartInfo = new CartInfo();
            // 补全数据
            cartInfo.setUserId(CartThreadLocalUtil.get());
            cartInfo.setSkuId(skuId);
            // 查询实时价格
            BigDecimal price = productFeign.getPrice(skuId);
            cartInfo.setCartPrice(price);
            cartInfo.setSkuNum(num);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            // 新增购物车
            int insert = cartInfoMapper.insert(cartInfo);
            if (insert <= 0) {
                throw new RuntimeException("新增购物车失败，请重试");
            }
        } else {    // 数量合并
            num = cartInfo.getSkuNum() + num;
            if (num <= 0) {
                cartInfoMapper.deleteById(cartInfo.getId());
            } else {
                cartInfo.setSkuNum(num);
                int update = cartInfoMapper.updateById(cartInfo);
                if (update < 0) {
                    throw new RuntimeException("修改购物车失败，请重试");
                }
            }
        }
    }

    /**
     * 查询指定用户的购物车数据
     *
     * @return
     */
    @Override
    public List<CartInfo> getCartInfo() {
        return cartInfoMapper.selectList(new LambdaQueryWrapper<CartInfo>().eq(CartInfo::getUserId, CartThreadLocalUtil.get()));
    }

    /**
     * 删除购物车
     * @param id
     */
    @Override
    public void removeCart(Long id) {
        int delete = cartInfoMapper.delete(new LambdaQueryWrapper<CartInfo>().eq(BaseEntity::getId, id).eq(CartInfo::getUserId, CartThreadLocalUtil.get()));
        if (delete < 0) {
            throw new RuntimeException("删除购物车失败！");
        }
    }

    /**
     * 修改购物车
     *
     * @param id
     * @param num
     */
    @Override
    public void updateCartNum(Long id, Integer num) {
        // 参数校验
        if (id == null) return ;
        if (num <= 0) { // 如果用户传递的数量<=0，则直接删除购物车
            int delete = cartInfoMapper.delete(new LambdaQueryWrapper<CartInfo>().eq(BaseEntity::getId, id).eq(CartInfo::getUserId, CartThreadLocalUtil.get()));
            if (delete < 0) {
                throw new RuntimeException("修改购物车失败！！！");
            }
        }
        // 直接将数据库中购物车的数量进行覆盖
        int i = cartInfoMapper.updateCartInfoNum(id, num, CartThreadLocalUtil.get());
        if (i < 0) {
            throw new RuntimeException("修改购物车失败！！！");
        }
    }

    /**
     * 修改选中状态
     *
     * @param id
     * @param status
     */
    @Override
    public void checkUpdate(Long id, Short status) {
        int i = 0;
        // 判断为全部操作还是单个操作
        if (id == null) {   // 全部操作
            i = cartInfoMapper.updateCheckAll(CartThreadLocalUtil.get(), status);

        } else {    // 单个操作
            i = cartInfoMapper.updateCheck(CartThreadLocalUtil.get(), status, id);
        }
        if (i < 0) {
            throw new RuntimeException("修改选中状态失败！！！");
        }
    }

    /**
     * 登录后调用：合并用户在未登录场景下添加的购物车数据
     *
     * @param cartInfoList
     */
    @Override
    public void mergeCart(List<CartInfo> cartInfoList) {
        // 合并购物车
        cartInfoList.stream().forEach(cartInfo -> {
            addCart(cartInfo.getSkuId(), cartInfo.getSkuNum());
        });
    }

    /**
     * 查询用户本次购买的购物车数据
     *
     * @return
     */
    @Override
    public Map<String, Object> getOrderConfirmCart() {
        // 返回结果初始化
        Map<String, Object> result = new HashMap<>();
        // 条件查询
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, CartThreadLocalUtil.get())
                        .eq(CartInfo::getIsChecked, CartConst.CART_CHECK));
        // 非空校验
        if (cartInfoList == null || cartInfoList.isEmpty()) {
            throw new RuntimeException("当前没有选中购物车数据");
        }
        // 计算总金额，总数量
        AtomicInteger totalNum = new AtomicInteger(0);
        AtomicDouble totalMoney = new AtomicDouble(0);
        List<CartInfo> cartInfoListNew = cartInfoList.stream().map(cartInfo -> {
            // 获取本笔购物车中商品的数量
            totalNum.getAndAdd(cartInfo.getSkuNum());
            // 获取本笔购物车中商品的单价（查询实时价格，而不是刚加入购物车时的价格）
            BigDecimal price = productFeign.getPrice(cartInfo.getSkuId());
            totalMoney.getAndAdd(price.doubleValue() * cartInfo.getSkuNum());
            // 保存实时价格
            cartInfo.setSkuPrice(price);
            // 返回结果
            return cartInfo;
        }).collect(Collectors.toList());
        // 保存结果
        result.put("totalNum", totalNum);
        result.put("totalMoney", totalMoney);
        result.put("cartInfoList", cartInfoListNew);
        // 返回结果
        return result;
    }

    /**
     * 删除购物车
     */
    @Override
    public void deleteCart() {
        int delete = cartInfoMapper.delete(new LambdaQueryWrapper<CartInfo>()
                .eq(CartInfo::getUserId, CartThreadLocalUtil.get())
                .eq(CartInfo::getIsChecked, CartConst.CART_CHECK));
        if (delete < 0) {
            throw new RuntimeException("删除购物车失败！！！");
        }
    }
}
