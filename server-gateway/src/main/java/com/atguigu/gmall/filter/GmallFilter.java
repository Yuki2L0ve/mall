package com.atguigu.gmall.filter;

import com.atguigu.gmall.common.util.IpUtil;
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
 * GmallFilter类实现了GlobalFilter接口，这意味着它是一个全局过滤器。同时实现了Ordered接口，这允许过滤器指定自己的执行顺序。
 * 这个全局过滤器的目的是确保所有通过网关的请求都携带了有效的token，并且这个token没有被盗用。
 * 这是一种安全措施，用于保护后端服务不受未授权访问。通过将token添加到请求头，它还可以被下游的服务用于进一步的身份验证和授权。
 */
@Component
public class GmallFilter implements GlobalFilter, Ordered {
    // 这是一个用于操作Redis字符串类型数据的模板类。
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 自定义的过滤逻辑
     * @param exchange  包含了请求和响应的信息。
     * @param chain chain是一个过滤器链，它允许过滤器继续执行链中的下一个过滤器。
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 从用户请求的参数中获取token
        ServerHttpRequest request = exchange.getRequest();
        // 获取响应体
        ServerHttpResponse response = exchange.getResponse();
        // 首先尝试从请求的查询参数中获取token
        String token = request.getQueryParams().getFirst("token");
        if (StringUtils.isEmpty(token)) {   // 如果查询参数中没有token，则尝试从请求头中获取。
            // 从请求头中获取token
            token = request.getHeaders().getFirst("token");
            if (StringUtils.isEmpty(token)) {   // 如果请求头中也没有token，则尝试从Cookie中获取。
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
        // 携带了令牌token，校验是否被盗用   使用IpUtil.getGatwayIpAddress(request)获取网关的IP地址。
        String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
        // 使用stringRedisTemplate从Redis中获取与网关IP地址关联的token。
        String redisToken = stringRedisTemplate.opsForValue().get(gatwayIpAddress);
        if (StringUtils.isEmpty(redisToken)) {  // 如果Redis中的token为空
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            // 没有携带令牌，拒绝掉
            return response.setComplete();
        }
        // 判断是否一致
        if (!redisToken.equals(token)) {    // 如果Redis中的token与请求中的token不一致
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            // 没有携带令牌，拒绝掉
            return response.setComplete();
        }
        // 需要将令牌以固定的key和固定的格式存储到request的请求头中去
        // 将token添加到请求头中，格式为Authorization: Bearer <token>。
        /**
         * mutate()方法的作用是创建一个新的ServerHttpRequest实例，这个实例包含了原始请求的所有信息，但是你可以修改它。
         * 这在需要修改请求头、查询参数或者路径等信息时非常有用。
         * mutate()方法通常在需要修改请求信息的过滤器或中间件中使用。例如，你可能需要在请求头中添加或修改某些信息，或者在请求路径中添加额外的参数。
         * 通过mutate()方法，你可以在不改变原始请求对象的情况下，创建一个新的请求对象来满足这些需求。
         */

        /**
         * Authorization字段是在HTTP请求头（Request Header）中的一个标准字段。它用于在客户端和服务器之间传递身份验证信息，以便服务器能够验证请求的合法性。
         * Authorization字段通常包含一个认证方案（如Basic、Bearer、Digest等）和认证凭证（如用户名和密码、令牌、密钥等）。
         * Authorization字段的作用和用途如下：
         * 1. 身份验证：Authorization字段允许客户端提供必要的凭证，以便服务器验证请求者的身份。这是实现安全通信的关键部分，尤其是在需要保护资源免受未授权访问的场景中。
         * 2. 访问控制：服务器可以根据Authorization字段中的信息来决定是否允许请求者访问特定的资源。这通常涉及到权限检查和角色验证。
         * 3. 无状态认证：在无状态的Web应用中，Authorization字段使得服务器能够在不依赖于服务器端会话的情况下进行身份验证。例如，使用OAuth 2.0协议时，客户端会携带一个访问令牌（access token）在Authorization头中，服务器通过验证这个令牌来确认客户端的权限。
         * 4. 简化客户端逻辑：客户端可以在请求头中包含Authorization字段，而无需在请求体中发送额外的身份验证信息。这简化了客户端的逻辑，并减少了请求体的大小。
         * 5. 兼容性：Authorization字段是HTTP协议的一部分，因此它在不同的服务器和客户端之间具有很好的兼容性。
         * 一个典型的Authorization请求头示例可能如下所示：Authorization: Basic QWxhZGRpbjpPcGVuU2VzYW1l
         * 在这个例子中，Basic是认证方案，后面的字符串是经过Base64编码的用户名和密码组合（username:password）。服务器会解码这个字符串，并验证提供的凭据是否正确。
         * 另一个例子是使用Bearer令牌的OAuth 2.0认证： Authorization: Bearer mF_9.B5f-4.1JqM
         * 在这个例子中，Bearer是认证方案，mF_9.B5f-4.1JqM是一个访问令牌。服务器会验证这个令牌的有效性，并根据令牌的权限来处理请求。
         */
        request.mutate().header("Authorization", "bearer " + token);
        // 放行   调用chain.filter(exchange)继续执行过滤器链中的下一个过滤器。
        return chain.filter(exchange);
    }

    /**
     * 全局过滤器的执行顺序   这是一个重写的方法，实现了Ordered接口中的getOrder方法。
     * 返回的整数值表示过滤器的执行顺序。数值越小，优先级越高，执行顺序越靠前。
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
