package com.example.mall.cart.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * 购物车微服务提供的内部调用feign接口
 */
@FeignClient(name = "service-cart", path = "/api/cart", contextId = "cartFeign")
public interface CartFeign {
    /**
     * 下单时调用查询实时购物车和实时总金额
     * @return
     */
    @GetMapping("/getOrderAddInfo")
    public Map<String, Object> getOrderAddInfo();

    /**
     * 购买下单后，清空购物车
     */
    @GetMapping("/deleteCart")
    public void deleteCart();
}
