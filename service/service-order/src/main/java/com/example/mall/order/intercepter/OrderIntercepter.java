package com.example.mall.order.intercepter;

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
 */
@Component
public class OrderIntercepter implements RequestInterceptor {
    /**
     * 触发时机：feign调用发生前
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 获取原始的请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            // 获取原始请求对象的请求体
            HttpServletRequest request = requestAttributes.getRequest();
            // 获取原始请求体的请求头中的所有参数
            Enumeration<String> headerNames = request.getHeaderNames();
            // 迭代
            while (headerNames.hasMoreElements()) {
                // 获取每个请求头的参数的名字
                String name = headerNames.nextElement();
                // 获取值
                String value = request.getHeader(name);
                // 存储到feign的http请求头中去
                requestTemplate.header(name, value);
            }
        }
    }
}
