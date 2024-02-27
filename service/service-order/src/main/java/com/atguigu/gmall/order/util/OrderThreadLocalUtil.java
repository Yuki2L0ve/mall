package com.atguigu.gmall.order.util;

/**
 * 订单微服务的本地线程工具类
 */
public class OrderThreadLocalUtil {

    //定义本地线程对象
    private final static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 获取用户名的方法
     * @return
     */
    public static String get(){
        return threadLocal.get();
    }

    /**
     * 存储方法
     * @param username
     */
    public static void set(String username){
        threadLocal.set(username);
    }
}
