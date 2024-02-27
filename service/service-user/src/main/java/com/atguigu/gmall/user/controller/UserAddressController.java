package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.user.service.UserAddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户收货地址相关的控制层
 */
@RestController
@RequestMapping("/api/user")
public class UserAddressController {
    @Resource
    private UserAddressService userAddressService;

    /**
     * 查询收货地址信息
     * @return
     */
    @GetMapping("/getUserAddress")
    public Result getUserAddress() {
        return Result.ok(userAddressService.getUserAddress());
    }
}
