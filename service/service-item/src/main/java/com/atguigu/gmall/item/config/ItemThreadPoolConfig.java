package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 商品详情微服务使用的自定义线程池
 */
@Configuration
public class ItemThreadPoolConfig {

    /**
     * 自定义线程池
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(24,   // 核心线程数: 如果是处理文件IO-->等于逻辑处理器数量  如果是处理计算-->等于逻辑处理器数量*2
                                    24,                // 最大线程数
                                    10,                 // 闲置时间
                                    TimeUnit.SECONDS,   // 闲置时间单位
                                    new ArrayBlockingQueue<>(10000),    // 阻塞队列
                                    Executors.defaultThreadFactory(),           // 线程工厂对象：创建线程
                                    new ThreadPoolExecutor.AbortPolicy());      // 拒绝策略
    }
}
