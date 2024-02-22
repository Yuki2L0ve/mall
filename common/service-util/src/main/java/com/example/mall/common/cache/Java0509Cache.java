package com.example.mall.common.cache;

import java.lang.annotation.*;

/**
 * 自定义一个注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Java0509Cache {
    String prefix() default "cache";
}
