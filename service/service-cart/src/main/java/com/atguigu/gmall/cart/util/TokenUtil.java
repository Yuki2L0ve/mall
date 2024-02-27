package com.atguigu.gmall.cart.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 这段代码定义了一个名为TokenUtil的Java类，它提供了两个静态方法，用于处理JWT（JSON Web Token）的解码和公钥的获取。
 * JWT是一种用于在客户端和服务器之间安全传输信息的格式。这个工具类主要用于验证JWT并从中提取用户信息。
 * 这个TokenUtil类的目的是在应用中提供一个统一的方式来处理JWT。它允许你验证JWT的签名，并从中提取用户信息，
 * 这对于实现基于JWT的身份验证和授权非常有用。通过使用公钥来验证JWT，可以确保JWT是由可信的服务器签发的，从而增加了安全性。
 */
public class TokenUtil {

    // 公钥   它包含了公钥文件的路径。这个公钥用于验证JWT的签名。
    private static final String PUBLIC_KEY = "public.key";
    // 用于存储公钥的内容
    private static String publickey = "";


    /**
     * 获取非对称加密公钥 Key
     *
     * @return 公钥 Key
     */
    public static String getPubKey() {
        // 检查publickey变量是否已经初始化。如果是，直接返回公钥。
        if (!StringUtils.isEmpty(publickey)) {
            return publickey;
        }
        // 如果publickey为空，方法尝试从类路径资源（ClassPathResource）中读取公钥文件。
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            // 使用InputStreamReader和BufferedReader读取公钥文件的内容，并将内容存储在publickey变量中。
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            publickey = br.lines().collect(Collectors.joining("\n"));
            // 最后，方法返回公钥。
            return publickey;
        } catch (IOException ioe) {
            return null;
        }
    }

    /***
     * 读取令牌数据   用于解码JWT并提取其中的数据
     */
    public static Map<String, String> decodeToken(String token) {
        // 校验Jwt    使用JwtHelper.decodeAndVerify方法验证JWT的签名。
        // 它接受JWT字符串和一个RsaVerifier（RSA验证器）作为参数。RsaVerifier使用getPubKey方法获取的公钥来验证JWT。
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(getPubKey()));
        // 获取Jwt原始内容    如果JWT验证成功，方法获取JWT的载荷（claims）
        String claims = jwt.getClaims();
        // 使用JSON.parseObject方法将载荷解析为一个Map<String, String>对象。最后，方法返回这个映射，它包含了JWT中的数据。
        return JSON.parseObject(claims, Map.class);
    }
}