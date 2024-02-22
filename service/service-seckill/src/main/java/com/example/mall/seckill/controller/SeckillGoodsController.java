package com.example.mall.seckill.controller;

import com.example.mall.common.result.Result;
import com.example.mall.seckill.service.SeckillGoodsService;
import com.example.mall.seckill.util.DateUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 秒杀商品的控制层
 */
@RestController
@RequestMapping("/seckill/goods")
public class SeckillGoodsController {
    @Resource
    private SeckillGoodsService seckillGoodsService;

    /**
     * 查询时间段菜单的接口
     * @return
     */
    @GetMapping("/getMenus")
    public Result getMenus() {
        return Result.ok(DateUtil.getDateMenus());
    }

    /**
     * 查询指定时间段的商品列表
     * @param time
     * @return
     */
    @GetMapping("/getSeckillGoods")
    public Result getSeckillGoods(String time) {
        return Result.ok(seckillGoodsService.getSeckillGoods(time));
    }

    /**
     * 查询商品的具体信息
     * @param time
     * @param goodsId
     * @return
     */
    @GetMapping("/getSeckillGood")
    public Result getSeckillGood(String time, String goodsId) {
        return Result.ok(seckillGoodsService.getSeckillGood(time, goodsId));
    }
}
