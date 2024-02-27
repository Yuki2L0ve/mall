package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/product")
public class TestController {
    @Autowired
    private TestService testService;

    /**
     * 测试方法
     * @return
     */
    @GetMapping
    public Result test() {
        //testService.setRedis();
        testService.setRedisByRedisson();
        return Result.ok();
    }
}
