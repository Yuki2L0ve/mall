package com.example.mall.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀使用的自定义线程池
 */
@Configuration
public class SeckillThreadPoolConfig {

    /**
     * 自定义线程池
     * @return
     */
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        return new ThreadPoolExecutor(24,
                24,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000));
    }
}
