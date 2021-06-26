package com.sr.assembly.lock.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 默认的重入集合
 * @author: sunrui62
 * @create: 2021/6/26 2:41 下午
 **/
public class DefaultReentryConcurrentMap implements ReentryMap{
    /**
     * 重入锁的次数记录
     */
    private static final ConcurrentHashMap<String, AtomicInteger> REENTRY_MAP = new ConcurrentHashMap<>();

    @Override
    public void put(String key, AtomicInteger value) {
        REENTRY_MAP.put(key, value);
    }

    @Override
    public AtomicInteger getOrDefault(String key, AtomicInteger defaultValue) {
        return REENTRY_MAP.getOrDefault(key, defaultValue);
    }

    @Override
    public AtomicInteger get(String key) {
        return REENTRY_MAP.get(key);
    }

    @Override
    public void remove(String key) {
        REENTRY_MAP.remove(key);
    }
}
