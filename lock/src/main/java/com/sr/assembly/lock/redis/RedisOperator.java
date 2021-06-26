package com.sr.assembly.lock.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * redis操作接口
 */
public abstract class RedisOperator {
    public abstract boolean setNx(String key, String value, Long expireTime);

    public abstract String get(String resource);

    public abstract void del(String resource);

    protected ThreadLocal<ConcurrentHashMap<String, AtomicInteger>> threadLocal(){
        return new ThreadLocal<ConcurrentHashMap<String, AtomicInteger>>();
    }
}
