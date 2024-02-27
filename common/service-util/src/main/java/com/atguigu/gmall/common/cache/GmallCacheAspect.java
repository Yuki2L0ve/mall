package com.atguigu.gmall.common.cache;

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
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 增强方法
     *
     * @param point 切点-->触发这个监听的时候，相关参数都在这个切点里面
     * @return
     * @Around 注解用于声明一个切面方法，这个切面方法会在匹配的连接点（通常是方法调用）前后执行。
     * 在方法执行前，你可以执行一些前置逻辑；在方法执行后，你可以执行一些后置逻辑，无论方法执行是否成功。
     * @annotation(com.atguigu.gmall.common.cache.Java0509Cache)用于指定切点。这个表达式匹配所有使用了 @Java0509Cache 注解的方法。
     * @annotation 是SpEL中的一个操作符，它用于匹配具有特定注解的方法。
     * point 是 ProceedingJoinPoint 类的一个实例，它代表了当前被代理的方法的连接点（即方法调用的上下文）。
     */
    @Around("@annotation(com.atguigu.gmall.common.cache.Java0509Cache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
        /*
        1.  获取参数列表
        2.  获取方法上的注解
        3.  获取前缀
        4.  获取目标方法的返回值
         */
        // 返回结果初始化
        Object result = null;
        try {
            // 获取方法的参数  point.getArgs() 方法用于获取当前方法调用的参数
            Object[] args = point.getArgs();
            // 获取方法的签名  point.getSignature() 方法返回一个 Signature 对象，这个对象包含了关于当前连接点（方法）的元数据信息，如方法名、参数类型、返回类型等。
            MethodSignature signature = (MethodSignature) point.getSignature();
            // 获取方法上的注解的对象
            // signature.getMethod() 方法返回一个 Method 对象，它代表了当前连接点对应的Java反射方法。
            // getAnnotation(Java0509Cache.class) 方法用于获取指定类型注解的实例。这里，我们获取了 Java0509Cache 注解的实例。
            // 如果方法上有这个注解，这个方法将返回注解的一个实例；如果没有，它将返回 null。
            Java0509Cache java0509Cache = signature.getMethod().getAnnotation(Java0509Cache.class);
            // 获取前缀  --> prefix = getSkuInfo:
            String prefix = java0509Cache.prefix();
            // 从缓存中获取数据  商品的key = getSkuInfo:[1]
            String key = prefix + Arrays.asList(args).toString();
            // 获取缓存数据
            result = cacheHit(signature, key);
            // 判断redis中是否取到了数据
            if (result != null) {
                // 缓存有数据
                return result;
            }
            // 走到这里说明redis中没有数据，需要线程开始抢锁，初始化分布式锁  锁的key = getSkuInfo:[1]:lock
            RLock lock = redissonClient.getLock(key + ":lock");
            if (lock.tryLock(100, 100, TimeUnit.SECONDS)) {
                try {
                    try {
                        // 加锁成功，才有资格运行方法     执行目标方法
                        result = point.proceed(point.getArgs());
                        // 若数据库中也没有数据    防止缓存穿透
                        if (null == result) {
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
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //boolean flag = lock.tryLock(10L, 10L, TimeUnit.SECONDS);
        return result;
    }

    /**
     * 从redis缓存中获取数据：从redis中通过key获取value，将value转换为方法返回的类型，然后返回结果。
     * 若redis中没有数据，则返回null
     * 它用于在Spring AOP切面中检查缓存是否存在，如果存在则返回缓存中的数据。
     *
     * @param signature
     * @param key       key = getSkuInfo:[1]
     * @return
     */
    private Object cacheHit(MethodSignature signature, String key) {
        // 1. 查询缓存
        String cache = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cache)) {
            // 有，则反序列化，直接返回     这行代码获取当前方法的返回类型。这在反序列化缓存数据时非常有用，因为我们需要知道预期的对象类型。
            Class returnType = signature.getReturnType(); // 获取方法返回类型 比如SkuInfo.class
            // 反序列化：将字符串转换为对象       不能使用parseArray<cache, T>，因为不知道List<T>中的泛型
            // 如果缓存中存在数据，这行代码将使用JSONObject的parseObject方法将字符串cache反序列化为指定的返回类型returnType的对象。
            return JSONObject.parseObject(cache, returnType);
        }
        return null;    // redis中没有数据
    }

}
