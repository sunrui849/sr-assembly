package com.sr.assembly.limit.annotation;

import java.lang.annotation.*;


/**
 * @author: sunrui
 * @create: 2021/6/23 3:29 下午
 * @description: 限流注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QpsLimit {
    /**
     * 限流组
     */
    String group();

    /**
     * 限流的qps
     */
    int qps();
}
