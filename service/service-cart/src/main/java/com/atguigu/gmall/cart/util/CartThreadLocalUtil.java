package com.atguigu.gmall.cart.util;

/**
 * 购物车为微服务的本地线程工具类
 * 这段代码定义了一个名为CartThreadLocalUtil的Java工具类，它使用ThreadLocal来为每个线程提供独立的存储空间。
 * 在这个特定的例子中，ThreadLocal被用来存储与当前线程相关的购物车信息，例如购物车ID。
 * 这种模式在Web应用程序中很常见，尤其是在处理用户会话和上下文信息时。
 * 这个类提供了与线程相关的购物车信息的存储和获取方法。
 */
public class CartThreadLocalUtil {

    // 定义本地线程对象 它用于存储当前线程的购物车信息。
    private final static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 获取用户名的方法
     * 这个方法调用threadLocal.get()来获取当前线程的ThreadLocal变量值。如果当前线程没有设置购物车信息，它将返回null。
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
