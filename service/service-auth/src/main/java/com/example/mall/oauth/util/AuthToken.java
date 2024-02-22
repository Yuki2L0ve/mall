package com.example.mall.oauth.util;

import lombok.Data;

import java.io.Serializable;

/**
 * 令牌实体类
 */
@Data
public class AuthToken implements Serializable{
    //令牌信息
    String accessToken;
    //刷新token(refresh_token)
    String refreshToken;
    //jwt短令牌
    String jti;
}