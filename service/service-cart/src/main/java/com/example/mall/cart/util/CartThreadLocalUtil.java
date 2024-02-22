package com.example.mall.cart.util;

/**
 * 购物车为微服务的本地线程工具类
 */
public class CartThreadLocalUtil {

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
