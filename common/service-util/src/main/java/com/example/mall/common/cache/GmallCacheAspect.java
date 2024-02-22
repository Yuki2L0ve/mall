package com.example.mall.common.cache;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 自定义的增强切面类
 */
@Component
@Aspect
public class mallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 增强方法
     * @param point 切点-->触发这个监听的时候，相关参数都在这个切点里面
     * @return
     */
    @Around("@annotation(com.example.mall.common.cache.Java0509Cache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
        // 返回结果初始化
        Object result = null;
        try {
            // 获取方法的参数
            Object[] args = point.getArgs();
            // 获取方法的签名
            MethodSignature signature = (MethodSignature) point.getSignature();
            // 获取方法上的注解的对象
            Java0509Cache java0509Cache = signature.getMethod().getAnnotation(Java0509Cache.class);
            // 获取前缀  --> prefix = getSkuInfo:
            String prefix = java0509Cache.prefix();
            // 从缓存中获取数据  商品的key = getSkuInfo:[1]
            String key = prefix + Arrays.asList(args).toString();
            // 获取缓存数据
            result = cacheHit(signature, key);
            // 判断redis中是否取到了数据
            if (result != null){
                // 缓存有数据
                return result;
            }
            // 走到这里说明redis中没有数据，需要线程开始抢锁，初始化分布式锁  锁的key = getSkuInfo:[1]:lock
            RLock lock = redissonClient.getLock(key + ":lock");
            if (lock.tryLock(100, 100, TimeUnit.SECONDS)){
               try {
                   try {
                       // 加锁成功，才有资格运行方法
                       result = point.proceed(point.getArgs());
                       // 若数据库中也没有数据    防止缓存穿透
                       if (null==result){
                           // 并把结果放入缓存,并设置有效期为5分钟
                           Object o = new Object();
                           this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(o), 5 * 60, TimeUnit.SECONDS);
                           return null;
                       }
                   } catch (Throwable throwable) {
                       throwable.printStackTrace();
                   }
                   // 并把结果放入缓存, 并设置有效期为1天
                   this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result), 60 * 60 * 24, TimeUnit.SECONDS);
                   // 返回数据库中的结果
                   return result;
               }catch (Exception e){
                   e.printStackTrace();
               }finally {
                   // 释放锁
                   lock.unlock();
               }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //boolean flag = lock.tryLock(10L, 10L, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 从redis缓存中获取数据：从redis中通过key获取value，将value转换为方法返回的类型，然后返回结果。
     * 若redis中没有数据，则返回null
     * @param signature
     * @param key  key = getSkuInfo:[1]
     * @return
     */
    private Object cacheHit(MethodSignature signature, String key) {
        // 1. 查询缓存
        String cache = (String)redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cache)) {
            // 有，则反序列化，直接返回
            Class returnType = signature.getReturnType(); // 获取方法返回类型 比如SkuInfo.class
            // 反序列化：将字符串转换为对象
            // 不能使用parseArray<cache, T>，因为不知道List<T>中的泛型
            return JSONObject.parseObject(cache, returnType);
        }
        return null;    // redis中没有数据
    }

}
