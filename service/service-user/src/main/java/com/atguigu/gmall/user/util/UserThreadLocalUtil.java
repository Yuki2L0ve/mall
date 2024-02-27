package com.atguigu.gmall.user.util;

/**
 * 购物车微服务的本地线程工具类
 * 这段代码定义了一个名为UserThreadLocalUtil的Java工具类，它使用了ThreadLocal来存储和获取与当前线程相关的用户信息。
 * ThreadLocal是Java中的一个特殊类，它提供了线程安全的变量存储，每个线程都有自己的变量副本，互不干扰。这在Web应用中特别有用，比如在处理用户会话信息时。
 */
public class UserThreadLocalUtil {

    // 定义本地线程对象
    private final static ThreadLocal<String> threadLocal = new ThreadLocal<>();

    /**
     * 获取用户名的方法
     * 这个方法调用threadLocal.get()来获取当前线程的ThreadLocal变量值。如果当前线程没有设置用户信息，它将返回null。
     * @return
     */
    public static String get(){
        return threadLocal.get();
    }

    /**
     * 存储方法
     * 这个方法调用threadLocal.set(username)来为当前线程设置用户信息。这通常在用户登录后调用，以便在后续的操作中可以获取当前用户的用户名。
     * @param username
     */
    public static void set(String username){
        threadLocal.set(username);
    }
}
