package com.example.mall.cart.controller;

import com.example.mall.cart.service.CartInfoService;
import com.example.mall.common.constant.CartConst;
import com.example.mall.common.result.Result;
import com.example.mall.model.cart.CartInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 购物车相关的控制层
 */
@RestController
@RequestMapping("/api/cart")
public class CartInfoController {
    @Resource
    private CartInfoService cartInfoService;

    /**
     * 新增购物车
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/addCart")
    public Result addCart(Long skuId, Integer num) {
        cartInfoService.addCart(skuId, num);
        return Result.ok();
    }

    /**
     * 查询指定用户的购物车数据
     * @return
     */
    @GetMapping("/getCartInfo")
    public Result getCartInfo() {
        return Result.ok(cartInfoService.getCartInfo());
    }

    /**
     * 删除购物车
     * @param id
     * @return
     */
    @GetMapping("/removeCart")
    public Result removeCart(Long id) {
        cartInfoService.removeCart(id);
        return Result.ok();
    }

    /**
     * 修改购物车数量
     * @param id
     * @param num
     * @return
     */
    @GetMapping("/updateCartNum")
    public Result updateCartNum(Long id, Integer num) {
        cartInfoService.updateCartNum(id, num);
        return Result.ok();
    }

    /**
     * 选中
     * @param id
     * @return
     */
    @GetMapping("/check")
    public Result check(Long id) {
        cartInfoService.checkUpdate(id, CartConst.CART_CHECK);
        return Result.ok();
    }

    /**
     * 取消选中
     * @param id
     * @return
     */
    @GetMapping("/uncheck")
    public Result uncheck(long id) {
        cartInfoService.checkUpdate(id, CartConst.CART_UNCHECK);
        return Result.ok();
    }

    /**
     * 登录后调用：合并用户在未登录场景下添加的购物车数据
     * @return
     */
    @PostMapping("/mergeCart")
    public Result mergeCart(List<CartInfo> cartInfoList) {
        cartInfoService.mergeCart(cartInfoList);
        return Result.ok();
    }

    /**
     * 获取订单确认页面相关的购物车信息
     * @return
     */
    @GetMapping("/getComfirmCart")
    public Result getComfirmCart() {
        return Result.ok(cartInfoService.getOrderConfirmCart());
    }

}
