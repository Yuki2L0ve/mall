package com.atguigu.gmall.oauth.config;

import com.atguigu.gmall.oauth.util.UserJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 这段代码是一个Spring Security OAuth2环境中的自定义用户认证转换器。它继承自DefaultUserAuthenticationConverter，
 * 用于将Spring Security的Authentication对象转换为OAuth2协议所需的用户认证信息。
 * 这个自定义转换器允许你控制转换过程中的行为，例如添加额外的用户信息到响应中。
 * DefaultUserAuthenticationConverter是Spring Security OAuth2提供的一个用于转换用户认证信息的类。
 */
@Component
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {
    // UserDetailsService是Spring Security中用于加载用户详细信息的服务。
    @Autowired
    UserDetailsService userDetailsService;

    // 这个方法用于将Authentication对象转换为一个Map，这个Map包含了用户认证信息。
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        // 创建一个新的LinkedHashMap实例，用于存储转换后的响应数据。
        LinkedHashMap response = new LinkedHashMap();
        // 从Authentication对象中获取用户名。
        String name = authentication.getName();
        // 将用户名添加到响应Map中，键为"username"。
        response.put("username", name);
        // 获取Authentication对象的主体（principal），这通常是用户的详细信息。
        Object principal = authentication.getPrincipal();
        // 创建一个UserJwt类型的变量，用于存储用户信息。
        UserJwt userJwt = null;
        // 检查主体是否是UserJwt的实例。如果是，直接将主体赋值给userJwt。
        if (principal instanceof UserJwt) {
            userJwt = (UserJwt) principal;
        } else {
            // refresh_token默认不去调用userdetailService获取用户信息，这里我们手动去调用，得到 UserJwt
            // 如果主体不是UserJwt的实例，使用userDetailsService加载用户详细信息，并将其转换为UserJwt。
            UserDetails userDetails = userDetailsService.loadUserByUsername(name);
            userJwt = (UserJwt) userDetails;
        }
        // 将用户的名称添加到响应Map中，键为"name"。
        response.put("name", userJwt.getName());
        // 将用户的ID添加到响应Map中，键为"id"。
        response.put("id", userJwt.getId());
        // 检查Authentication对象是否有权限信息。如果有，将权限信息添加到响应Map中，键为"authorities"。
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put("authorities", AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        // 返回包含用户认证信息的Map。
        return response;
    }

}
