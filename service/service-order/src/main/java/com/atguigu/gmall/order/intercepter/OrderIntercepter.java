package com.atguigu.gmall.order.intercepter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 订单微服务的拦截器
 * 过滤器：Servlet组件 --> 进入微服务的请求
 * 拦截器：SpringMVC组件 --> 从微服务出去的请求
 * 用于在Feign客户端发起HTTP请求之前拦截并修改请求。RequestInterceptor是Feign提供的一个拦截器接口，允许开发者在请求被发送之前对其进行自定义处理。
 * OrderIntercepter类实现了RequestInterceptor接口，这意味着它可以在Feign请求执行前进行拦截。
 * 这个方法的作用是将原始HTTP请求的所有头信息复制到Feign客户端发起的请求中。
 * 这在某些情况下非常有用，比如当你需要在Feign请求中保留原始请求的认证信息、追踪标识或其他重要的请求头时。
 */
@Component
public class OrderIntercepter implements RequestInterceptor {
    /**
     * 触发时机：feign调用发生前  这个方法在Feign客户端发起请求之前被调用。
     * @param requestTemplate   RequestTemplate参数包含了Feign请求的配置信息，包括URL、HTTP方法、请求头、请求体等。
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 获取原始的请求对象
        // 使用RequestContextHolder.getRequestAttributes()获取当前请求的ServletRequestAttributes对象。这个对象包含了原始的请求信息。
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            // 获取原始请求对象的请求体
            HttpServletRequest request = requestAttributes.getRequest();
            // 获取原始请求体的请求头中的所有参数    获取原始请求的所有头名称。
            Enumeration<String> headerNames = request.getHeaderNames();
            // 迭代
            while (headerNames.hasMoreElements()) {
                // 获取每个请求头的参数的名字
                String name = headerNames.nextElement();
                // 对于每个请求头，使用request.getHeader(name)获取其值。
                String value = request.getHeader(name);
                // 存储到feign的http请求头中去   将原始请求头的名称和值添加到Feign请求的配置中。
                requestTemplate.header(name, value);
            }
        }
    }
}
