package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 操作redis
     */
    @Override
    public void setRedis() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        // 使用redis的setnx命令尝试加锁，只有加锁成功的线程才能操作redis中java0509这个key
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", "uuid", 5, TimeUnit.SECONDS);
        try {
            if (lock) { // 加锁成功
                try {
                    // 从redis中获取一个key
                    Integer java0509 = (Integer) redisTemplate.opsForValue().get("java0509");
                    // 如果这个key的value不为空，则进行自增操作
                    if (java0509 != null) {
                        java0509++;
                        // 操作完成以后，将结果重新写入到redis中
                        redisTemplate.opsForValue().set("java0509", java0509);
                    }
                    // int i = 1 / 0;   // 出现异常
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
//                    // 获取锁的value
//                    String redisUUID = (String) redisTemplate.opsForValue().get("lock");
//                    // 判断锁释放为自己的，只有自己才能释放自己的锁
//                    if (redisUUID.equals(uuid)) {
//                        // 释放锁
//                        redisTemplate.delete("lock");
//                    }

                    // 使用lua脚本来释放锁
                    // 脚本初始化
                    DefaultRedisScript script = new DefaultRedisScript();
                    // 设置脚本
                    script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
                    // 设置返回结果类型
                    script.setResultType(Long.class);
                    // 执行释放锁
                    redisTemplate.execute(script, Arrays.asList("lock"), uuid);
                }
            } else {    // 加锁失败
                Thread.sleep(100);
                setRedis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 操作redis使用Redisson加锁
     */
    @Override
    public void setRedisByRedisson() {
        // 获取锁
        RLock lock = redissonClient.getLock("lock");
        // 尝试加锁
        /**
         * lock.lock(): 在这里加锁，直到成功为止
         * lock.tryLock(): 只尝试一次
         * lock.tryLock(10, TimeUnit.SECONDS)，指定时间内一直尝试加锁，除非超时
         * lock.tryLock(10, 10, TimeUnit.SECONDS)，指定时间内一直尝试加锁，除非超时，加锁成功后锁的有效期是多久
         */
        try {
            if (lock.tryLock(10, 10, TimeUnit.SECONDS)) {
                // 加锁成功
                try {
                    // 加锁成功的操作
                    // 从redis中获取一个key
                    Integer java0509 = (Integer) redisTemplate.opsForValue().get("java0509");
                    // 如果这个key的value不为空，则进行自增操作
                    if (java0509 != null) {
                        java0509++;
                        // 操作完成以后，将结果重新写入到redis中
                        redisTemplate.opsForValue().set("java0509", java0509);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("加锁成功，但是Java代码执行出现异常");
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("加锁失败");
        }
    }
}
