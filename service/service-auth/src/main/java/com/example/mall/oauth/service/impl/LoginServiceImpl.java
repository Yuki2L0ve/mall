package com.example.mall.oauth.service.impl;

import com.example.mall.oauth.service.LoginService;
import com.example.mall.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.Map;

/**
 * 自定义登录接口的实现类
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private LoadBalancerClient loadBalancerClient;
    @Value("${auth.clientId}")
    private String clientId;
    @Value("${auth.clientSecret}")
    private String clientSecret;

    /**
     * 自定义登录
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public AuthToken login(String username, String password) {
        // 校验参数
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new RuntimeException("用户名或密码不能为空！！！");
        }
        // 请求的url
        ServiceInstance serviceInstance = loadBalancerClient.choose("service-oauth");
        String url = serviceInstance.getUri().toString() + "/oauth/token";
        //String url = "http://localhost:9001/oauth/token";

        // 发送post请求
        // 请求头初始化
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.set("Authorization", getHeaderParam());
        // 请求体初始化
        MultiValueMap<String, String> body = new HttpHeaders();
        body.set("username", username);
        body.set("password", password);
        body.set("grant_type", "password");
        // 请求参数对象初始化
        HttpEntity httpEntity = new HttpEntity(body, headers);
        // 发送post请求
        /**
         * 1. 请求地址
         * 2. 请求方法类型
         * 3. 请求参数
         * 4. 返回结果类型
         */
        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
        // 获取结果
        Map<String, String> result = exchange.getBody();
        AuthToken authToken = new AuthToken();
        // 获取令牌access_token
        String accessToken = result.get("access_token");
        authToken.setAccessToken(accessToken);
        // 获取刷新令牌refresh_token
        String refreshToken = result.get("refresh_token");
        authToken.setRefreshToken(refreshToken);
        // 获取唯一标识
        String jti = result.get("jti");
        authToken.setJti(jti);
        // 返回结果
        return authToken;
    }

    /**
     * 拼接请求头中的参数
     * @return
     */
    public String getHeaderParam() {
        String result = "Basic ";
        // 拼接
        String a = clientId + ":" + clientSecret;
        // base64加密
        byte[] encode = Base64.getEncoder().encode(a.getBytes());
        return result + new String(encode);
    }
}
