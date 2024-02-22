package com.example.mall.cart.controller;


import com.example.mall.cart.service.CartInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 购物车微服务提供给内部调用的控制层
 */
@RestController
@RequestMapping("/api/cart")
public class CartForOrderController {
    @Resource
    private CartInfoService cartInfoService;

    /**
     * 下单时调用查询实时购物车和实时总金额
     * @return
     */
    @GetMapping("/getOrderAddInfo")
    public Map<String, Object> getOrderAddInfo() {
        return cartInfoService.getOrderConfirmCart();
    }

    /**
     * 购买下单后，清空购物车
     */
    @GetMapping("/deleteCart")
    public void deleteCart() {
        cartInfoService.deleteCart();
    }
}
