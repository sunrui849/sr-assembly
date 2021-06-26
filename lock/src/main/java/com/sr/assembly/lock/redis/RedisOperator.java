package com.sr.assembly.lock.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * redis操作接口
 */
public abstract class RedisOperator {
    /**
     * 重入锁的次数记录
     */
    private static final ConcurrentHashMap<String, AtomicInteger> REENTRY_MAP = new ConcurrentHashMap<>();

    public abstract boolean setNx(String key, String value, Long expireTime);

    public abstract String get(String resource);

    public abstract void del(String resource);

    public ReentryMap reentryMap() {
        return new DefaultReentryConcurrentMap();
    }
}
