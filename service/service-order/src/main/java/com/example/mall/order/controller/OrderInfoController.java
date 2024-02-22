package com.example.mall.order.controller;

import com.example.mall.common.result.Result;
import com.example.mall.model.order.OrderInfo;
import com.example.mall.order.service.OrderInfoService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 订单的控制层
 */
@RestController
@RequestMapping("/api/order")
public class OrderInfoController {
    @Resource
    private OrderInfoService orderInfoService;

    /**
     * 新增订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/addOrder")
    public Result addOrder(@RequestBody OrderInfo orderInfo) {
        orderInfoService.addOrder(orderInfo);
        return Result.ok();
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     */
    @GetMapping("/cancelOrder")
    public Result cancelOrder(Long orderId) {
        orderInfoService.cancelOrder(orderId);
        return Result.ok();
    }
}
