package com.example.mall.user.filter;

import com.example.mall.user.util.UserThreadLocalUtil;
import com.example.mall.user.util.TokenUtil;
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
 * 用户微服务的过滤器
 */
@WebFilter(filterName = "userFilter", urlPatterns = "/*")
@Order(1)   // 过滤器的执行顺序
public class UserFilter extends GenericFilterBean {
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
            UserThreadLocalUtil.set(username);
        }
        // 放行
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
