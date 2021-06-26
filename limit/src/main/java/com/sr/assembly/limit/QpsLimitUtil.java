package com.sr.assembly.limit;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: 限流工具类 按qps进行限流 单机限流
 * @author: sunrui
 * @create: 2021/6/9 6:09 下午
 **/
public class QpsLimitUtil {
    private static final ConcurrentMap<String, AtomicInteger> LIMIT_MAP = Caffeine.newBuilder()
            .expireAfterAccess(3, TimeUnit.SECONDS)
            .initialCapacity(3000) // 限流容量，可以调整
            .maximumSize(3000)
            .build(new CacheLoader<String, AtomicInteger>() {
                @Override
                public @Nullable AtomicInteger load(@NonNull String key) throws Exception {
                    return null;
                }
            }).asMap();

    private QpsLimitUtil(){}

    /**
     * 限流，每次加 num
     * @param key 限流的key
     * @param num 每次增加流量
     * @param limit 限流大小，每秒调用次数
     * @throws LimitException
     */
    public static void limit(String key, int num, int limit) throws LimitException {
        long time = System.currentTimeMillis() / 1000;
        String limitKey = key.concat(String.valueOf(time));
        AtomicInteger limitNum = LIMIT_MAP.putIfAbsent(limitKey, new AtomicInteger());
        if (limitNum == null) {
            limitNum = LIMIT_MAP.get(limitKey);
        }
        int numSecond = limitNum.addAndGet(num);
        if (numSecond > limit) {
            throw new LimitException(key + " 被限流， 每秒达到" + numSecond);
        }
    }

    /**
     * 限流, 每次加1
     *
     * @param key   限流的key
     * @param limit 限流大小，每秒调用次数
     * @throws LimitException
     */
    public static void limit(String key, int limit) throws LimitException {
        limit(key, 1, limit);
    }
}
