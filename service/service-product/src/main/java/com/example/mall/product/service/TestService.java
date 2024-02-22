package com.example.mall.product.service;

/**
 * 测试的接口类
 */
public interface TestService {
    /**
     * 操作redis
     */
    public void setRedis();

    /**
     * 操作redis使用Redisson加锁
     */
    public void setRedisByRedisson();
}
