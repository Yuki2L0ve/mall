package com.atguigu.gmall.common.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 获取ip地址
 */
public class IpUtil {
    /**
     * getIpAddress(HttpServletRequest request)用于从HTTP请求中获取客户端的IP地址.
     * 在Web应用中，获取客户端IP地址是一个常见的需求，尤其是在需要记录日志、进行地理位置分析或实现基于IP的访问控制时。
     * 由于客户端可能通过一个或多个代理服务器连接到Web服务器，直接获取的IP地址可能不是客户端的真实IP。
     * 这段代码尝试从多个HTTP头中获取IP地址，并处理可能的代理情况。
     * @param request   它接受一个HttpServletRequest对象作为参数，这个对象包含了HTTP请求的信息。
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        // ipAddress用于存储客户端的IP地址,初始值为null。
        String ipAddress = null;
        try {
            /**
             * 尝试从HTTP请求头中获取IP地址：方法尝试从X-Forwarded-For、Proxy-Client-IP和WL-Proxy-Client-IP这些HTTP头中获取IP地址。
             * 这些头通常由代理服务器添加，用于传递客户端的真实IP地址。如果这些头中的IP地址为空、长度为0或者等于"unknown"（表示未知），
             * 则尝试获取request对象的getRemoteAddr()方法返回的IP地址，这是服务器接收到的IP地址。
             */
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1")) {    // 处理本地回环地址（127.0.0.1）：
                    // 如果IP地址是127.0.0.1，这通常意味着请求来自本地服务器。在这种情况下，代码尝试获取本机配置的IP地址。
                    // 这通过调用InetAddress.getLocalHost()方法实现，然后通过getHostAddress()获取该地址的主机地址。
                    InetAddress inet = null;    // 根据网卡取本机配置的IP
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    ipAddress = inet.getHostAddress();
                }
            }
            /**
             * 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
             * 如果ipAddress不为空且长度大于15（一个IP地址的标准长度），代码检查是否包含逗号分隔符。
             * 如果包含，说明可能通过多个代理，代码将IP地址截取到第一个逗号之前的部分，这通常代表客户端的真实IP地址。
             */
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            // 在尝试获取IP地址的过程中，如果发生异常（如网络问题或配置错误），异常被捕获，并将ipAddress设置为空字符串。
            ipAddress = "";
        }
        // ipAddress = this.getRequest().getRemoteAddr();

        // 最后，方法返回ipAddress变量的值，这将是客户端的IP地址或者在获取失败时的空字符串。
        return ipAddress;
    }

    /**
     * 这段代码用于从Spring WebFlux中的ServerHttpRequest对象获取网关的IP地址。
     * 在分布式系统中，尤其是在使用了负载均衡器或反向代理的情况下，直接获取的IP地址可能不是客户端的真实IP地址。
     * 为了获取真实的客户端IP地址，通常会检查一系列的HTTP头。这个方法尝试从多个HTTP头中获取IP地址，
     * 如果这些头中没有有效的IP地址，它会退回到使用ServerHttpRequest对象的远程地址。
     * 这个方法的逻辑是尝试从多个可能的HTTP头中获取客户端的IP地址，如果这些头中没有有效的IP地址，它会退回到使用请求的远程地址。
     * 这种方法可以提高在复杂网络环境下获取客户端真实IP地址的准确性。
     * @param request
     * @return
     */
    public static String getGatwayIpAddress(ServerHttpRequest request) {
        // 获取请求的HTTP头信息，存储在HttpHeaders对象中。
        HttpHeaders headers = request.getHeaders();
        // 尝试从X-Forwarded-For头中获取IP地址。这个头通常包含了客户端IP地址，以及可能的代理服务器IP地址。
        String ip = headers.getFirst("x-forwarded-for");
        // 如果X-Forwarded-For头存在且不为空，代码会检查是否包含逗号分隔符。
        // 如果包含，说明可能经过了多个代理，代码将IP地址截取为第一个逗号之前的部分，这通常代表客户端的真实IP地址。
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        /**
         * 如果X-Forwarded-For头无效，尝试从其他HTTP头获取IP地址：
         * 方法依次尝试从Proxy-Client-IP、WL-Proxy-Client-IP、HTTP_CLIENT_IP、
         * HTTP_X_FORWARDED_FOR和X-Real-IP这些头中获取IP地址。这些头可能由代理服务器添加，用于传递客户端的真实IP地址。
         */
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        /**
         * 如果所有HTTP头都无效，使用请求的远程地址：如果上述所有尝试都失败，代码会使用
         * request.getRemoteAddress().getAddress().getHostAddress()获取服务器接收到的IP地址。这通常是代理服务器或负载均衡器的IP地址。
         */
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        // 最后，方法返回ip变量的值，这将是客户端的IP地址或者在获取失败时的服务器接收到的IP地址。
        return ip;
    }
}