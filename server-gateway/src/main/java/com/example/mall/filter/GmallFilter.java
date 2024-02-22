package com.example.mall.filter;

import com.example.mall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 网关全局过滤器定义
 */
@Component
public class mallFilter implements GlobalFilter, Ordered {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 自定义的过滤逻辑
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 从用户请求的参数中获取token
        ServerHttpRequest request = exchange.getRequest();
        // 获取响应体
        ServerHttpResponse response = exchange.getResponse();
        // 从url中获取token
        String token = request.getQueryParams().getFirst("token");
        if (StringUtils.isEmpty(token)) {
            // 从请求头中获取token
            token = request.getHeaders().getFirst("token");
            if (StringUtils.isEmpty(token)) {
                // 从Cookie中获取token
                MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                if (cookies != null) {
                    HttpCookie cookiesFirst = cookies.getFirst("token");
                    if (cookiesFirst != null) {
                        token = cookiesFirst.getValue();
                    }
                }
            }
        }
        // 判断是否取到了token
        if (StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            // 没有携带令牌，拒绝掉
            return response.setComplete();
        }
        // 携带了令牌token，校验是否被盗用
        String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
        String redisToken = stringRedisTemplate.opsForValue().get(gatwayIpAddress);
        if (StringUtils.isEmpty(redisToken)) {
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            // 没有携带令牌，拒绝掉
            return response.setComplete();
        }
        // 判断是否一致
        if (!redisToken.equals(token)) {
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            // 没有携带令牌，拒绝掉
            return response.setComplete();
        }
        // 需要将令牌以固定的key和固定的格式存储到request的请求头中去
        request.mutate().header("Authorization", "bearer " + token);
        // 放行
        return chain.filter(exchange);
    }

    /**
     * 全局过滤器的执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
