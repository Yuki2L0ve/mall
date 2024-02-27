package com.atguigu.gmall.oauth.service;

import com.atguigu.gmall.oauth.util.AuthToken;

/**
 * 用户登录的接口类
 */
public interface LoginService {

    /**
     * 自定义登录
     *
     * @param username
     * @param password
     * @return
     */
    public AuthToken login(String username, String password);
}
