package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.cart.util.CartThreadLocalUtil;
import com.atguigu.gmall.common.constant.CartConst;
import com.atguigu.gmall.model.base.BaseEntity;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.feign.ProductFeign;
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
        /** 判断用户购物车中是否已经有了这个商品
         * selectOne是一个方法，用于根据给定的条件查询数据库，并返回一个结果对象。如果查询结果有多条记录，selectOne会抛出异常。通常用于查询单条记录的场景。
         * 这段代码的逻辑是：在数据库中查询是否存在一个CartInfo记录，其skuId字段等于指定的skuId，且userId字段等于当前线程关联的用户ID。
         * 如果这样的记录存在，那么说明用户购物车中已经包含了这个商品。如果不存在，那么购物车中没有这个商品。
         */
        CartInfo cartInfo = cartInfoMapper.selectOne(
                new LambdaQueryWrapper<CartInfo>().
                        eq(CartInfo::getSkuId, skuId).
                        eq(CartInfo::getUserId, CartThreadLocalUtil.get()));
        if (cartInfo == null) { // 新增
            // 用户第一次添加该商品到购物车时，如果数量<=0直接return
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
            if (num <= 0) { // 数量<=0说明用户不想购买这个商品，当然要在购物车中清除这个商品
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
        /**
         * 在MyBatis Plus中，delete方法的返回值表示的是受影响的行数。
         * 1. 通常情况下，如果返回值大于0，表示有记录被成功删除；
         * 2. 如果返回值为0，表示没有记录匹配给定的条件，即没有记录被删除；
         * 3. 如果返回值小于0，通常表示发生了错误，如SQL语法错误、数据库连接问题或其他异常情况。
         * 这里判断delete < 0 而不是 delete <= 0 的原因：
         * 如果返回值是0，这通常意味着没有记录被删除，这可能是一个正常的情况（例如，购物车中没有指定ID的商品）。
         * 而返回值小于0则表示发生了异常，这是一个非预期的状态，需要通过异常处理来通知调用者。
         */
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
        int update = cartInfoMapper.updateCartInfoNum(id, num, CartThreadLocalUtil.get());
        if (update < 0) {
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
        /** 判断为全部操作还是单个操作
         * 为什么id == null表示全部操作，而id不为null表示单个操作？
         * 这种设计通常是基于业务逻辑的考虑。在购物车系统中，可能需要提供两种操作模式：
         * 1. 全部操作：用户可能想要更新购物车中所有商品的选中状态，例如，用户想要全选或全不选所有商品。在这种情况下，不需要指定商品ID，因为操作适用于所有商品。
         * 2. 单个操作：用户可能只想更新购物车中某个特定商品的选中状态。在这种情况下，需要指定商品ID，以便只更新那一个商品。
         */
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
     * 用于查询用户当前选中的购物车数据，并计算总金额和总数量。这个方法可能是在一个购物车服务的实现类中，用于在用户准备下单时获取订单确认页面所需的数据。
     * @return
     */
    @Override
    public Map<String, Object> getOrderConfirmCart() {
        // 返回结果初始化
        Map<String, Object> result = new HashMap<>();
        // 条件查询
        // 使用cartInfoMapper.selectList方法查询数据库，获取用户当前选中的购物车项列表。
        // 查询条件包括用户ID（通过CartThreadLocalUtil.get()获取）和购物车项的选中状态（CART_CHECK）。
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(
                new LambdaQueryWrapper<CartInfo>()
                        .eq(CartInfo::getUserId, CartThreadLocalUtil.get())
                        .eq(CartInfo::getIsChecked, CartConst.CART_CHECK));
        // 非空校验
        if (cartInfoList == null || cartInfoList.isEmpty()) {
            throw new RuntimeException("当前没有选中购物车数据");
        }
        // 计算总金额，总数量
        // 使用AtomicInteger和AtomicDouble来确保在并发环境下对总数和总金额的累加操作是线程安全的。
        AtomicInteger totalNum = new AtomicInteger(0);
        AtomicDouble totalMoney = new AtomicDouble(0);
        List<CartInfo> cartInfoListNew = cartInfoList.stream().map(cartInfo -> {
            // 获取本笔购物车中商品的数量    使用getAndAdd方法累加商品数量到totalNum。
            totalNum.getAndAdd(cartInfo.getSkuNum());
            // 获取本笔购物车中商品的单价（查询实时价格，而不是刚加入购物车时的价格）
            // 调用productFeign.getPrice方法获取商品的实时价格，并累加到totalMoney。
            BigDecimal price = productFeign.getPrice(cartInfo.getSkuId());
            totalMoney.getAndAdd(price.doubleValue() * cartInfo.getSkuNum());
            // 保存实时价格   更新购物车项的skuPrice属性为实时价格。
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
