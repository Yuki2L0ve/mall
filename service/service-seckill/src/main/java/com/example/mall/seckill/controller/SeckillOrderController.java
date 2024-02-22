package com.example.mall.seckill.controller;

import com.example.mall.common.result.Result;
import com.example.mall.seckill.service.SeckillOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 秒杀下单的控制层
 */
@RestController
@RequestMapping("/seckill/order")
public class SeckillOrderController {
    @Resource
    private SeckillOrderService seckillOrderService;

    /**
     *
     * @param time
     * @param goodsId
     * @param num
     * @return
     */
    @GetMapping("/addSeckillOrder")
    public Result addSeckillOrder(String time, String goodsId, Integer num) {
        return Result.ok(seckillOrderService.addSeckillOrder(time, goodsId, num));
    }

    /**
     * 获取用户的排队状态
     * @return
     */
    @GetMapping(value = "/getUserRecode")
    public Result getUserRecode(){
        return Result.ok(seckillOrderService.getUserRecode());

    }

    /**
     * 用户主动取消订单
     * @return
     */
    @GetMapping("/cancelSeckillOrder")
    public Result cancelSeckillOrder() {
        seckillOrderService.cancelSeckillOrder(null);
        return Result.ok();
    }
}
