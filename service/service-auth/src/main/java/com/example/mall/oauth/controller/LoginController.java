package com.example.mall.oauth.controller;

import com.example.mall.common.result.Result;
import com.example.mall.common.util.IpUtil;
import com.example.mall.oauth.service.LoginService;
import com.example.mall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user/login")
public class LoginController {
    @Resource
    private LoginService loginService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 自定义登录
     * @param username
     * @param password
     * @return
     */
    @GetMapping
    public Result login(String username, String password, HttpServletRequest request) {
        // 登录获取令牌
        AuthToken login = loginService.login(username, password);
        // 将令牌和IP地址绑定，获取用户的IP地址
        String ipAddress = IpUtil.getIpAddress(request);
        // 将用户id和令牌绑定关系到redis中
        stringRedisTemplate.opsForValue().set(ipAddress, login.getAccessToken());
        // 返回
        return Result.ok(login);
    }
}
