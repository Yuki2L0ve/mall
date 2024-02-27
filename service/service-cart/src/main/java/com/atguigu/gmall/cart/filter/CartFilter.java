package com.atguigu.gmall.cart.filter;

import com.atguigu.gmall.cart.util.CartThreadLocalUtil;
import com.atguigu.gmall.cart.util.TokenUtil;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * 购物车微服务的过滤器
 * 它是一个Web过滤器，用于在Spring Web应用中处理HTTP请求。
 * 这个特定的过滤器用于处理购物车微服务的相关逻辑，例如验证请求中的令牌（token）并存储用户信息。
 * @WebFilter这是一个Java EE的注解，用于声明一个Web过滤器。filterName属性为过滤器提供了一个名称（"cartFilter"）。
 * urlPatterns属性定义了过滤器将要拦截的URL模式。"/*"表示过滤器将拦截所有进入应用的请求。
 * @Order(1)这是一个Spring框架的注解，用于指定过滤器的执行顺序。Order注解的值越小，过滤器的执行顺序越靠前.
 * GenericFilterBean这是Spring框架提供的一个通用过滤器基类，它实现了Filter接口。
 * 这个过滤器的目的是在请求处理流程中验证Bearer令牌，并从中提取用户信息。
 * 一旦验证成功，它将用户信息存储在ThreadLocal中，以便在整个请求处理过程中使用。
 * 这种模式在Web应用中很常见，尤其是在需要跨多个组件传递用户上下文信息的场景中。
 */
@WebFilter(filterName = "cartFilter", urlPatterns = "/*")
@Order(1)   // 过滤器的执行顺序
public class CartFilter extends GenericFilterBean {
    /**
     * 自定义过滤器的逻辑
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        // 获取请求头中的参数    将ServletRequest对象转换为HttpServletRequest对象，以便访问HTTP请求的特定功能。
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 获取令牌 从请求头中获取名为"Authorization"的值，这通常包含了一个Bearer令牌。然后，使用replace方法移除令牌字符串中的"bearer "前缀，以便后续处理。
        String token = request.getHeader("Authorization").replace("bearer ", "");
        // 用于解析JWT令牌并提取其中的载荷（payload）数据。解码后的令牌数据被存储在一个Map对象中。
        Map<String, String> map = TokenUtil.decodeToken(token);
        // 检查解码后的令牌数据（map）是否不为null。如果不为null，表示令牌解码成功。
        if (map != null) {
            // 获取用户名    从解码后的令牌数据中获取用户名。
            String username = map.get("username");
            // 存储用户名    使用CartThreadLocalUtil工具类的方法set来存储用户名。这允许在请求处理过程中的其他部分访问当前用户的用户名。
            CartThreadLocalUtil.set(username);
        }
        // 放行   调用filterChain.doFilter方法，继续执行过滤器链中的下一个过滤器。
        filterChain.doFilter(servletRequest, servletResponse);
    }
}