package com.sr.assembly.lock.redis;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 重入映射集合
 * @author: sunrui62
 * @create: 2021/6/26 2:40 下午
 **/
public interface ReentryMap {
    void put(String key, AtomicInteger value);
    AtomicInteger getOrDefault(String key, AtomicInteger defaultValue);
    AtomicInteger get(String key);
    void remove(String key);
}
