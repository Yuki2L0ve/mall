package com.example.mall.order.filter;

import com.example.mall.order.util.OrderThreadLocalUtil;
import com.example.mall.order.util.TokenUtil;
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
 * 订单微服务的过滤器
 */
@WebFilter(filterName = "orderFilter", urlPatterns = "/*")
@Order(1)   // 过滤器的执行顺序
public class OrderFilter extends GenericFilterBean {
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
        // 获取请求头中的参数
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 获取令牌
        String token = request.getHeader("Authorization").replace("bearer ", "");
        // 解析令牌中的载荷中的数据
        Map<String, String> map = TokenUtil.decodeToken(token);
        if (map != null) {
            // 获取用户名
            String username = map.get("username");
            // 存储用户名
            OrderThreadLocalUtil.set(username);
        }
        // 放行
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
