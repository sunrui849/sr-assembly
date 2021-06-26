package com.sr.assembly.lock.redis;

import com.sr.assembly.lock.LockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description: redis实现分布式锁
 * @author: sunrui
 * @create: 2021/6/23 11:09 上午
 **/
@Slf4j
@Component
@ConditionalOnProperty(prefix = "com.sr.assembly.lock", name = "redis", havingValue = "true")
public class RedisLock {
    /**
     * 空值
     */
    private static final String NONE = "NONE";

    /**
     *
     */
    private static final AtomicInteger DEFAULT_ZERO = new AtomicInteger(0);

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private RedisProperties redisProperties;

    /**
     * 尝试获取锁
     *
     * @param resource   资源ID
     * @param threadId   线程ID
     * @param expireTime 过期时间 毫秒
     * @return
     */
    public boolean tryLock(String resource, String threadId, Long expireTime) {
        if (StringUtils.isBlank(resource) || !numberLessAndEqualsThanZero(expireTime)) {
            throw new LockException("unlock param is error. resource can not be blank & expireTime must greater zero.");
        }

        threadId = buildThreadId(threadId);
        return redisOperator.setNx(resource, threadId, expireTime);
    }

    /**
     * 阻塞获取可重入锁
     *
     * @param resource   资源ID
     * @param threadId   线程ID
     * @param waitTime   等待时间 毫秒
     * @param expireTime 过期时间 毫秒 过期时间以第一个获取到锁的为准，需要业务谨慎考虑设置过期时间
     * @return
     */
    @Deprecated
    public boolean reentryLock(String resource, String threadId, Long waitTime, Long expireTime) {
        if (StringUtils.isBlank(resource) || numberLessAndEqualsThanZero(waitTime) || numberLessAndEqualsThanZero(expireTime)) {
            throw new LockException("unlock param is error. resource can not be blank & waitTime and expireTime must greater zero.");
        }

        threadId = buildThreadId(threadId);

        // 检查是否可重入
        if (reentryLock(resource, threadId) > 1){
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long outTime = currentTime + waitTime;
        while (currentTime < outTime) {
            // 检查是否可重入
            if (reentryLock(resource, threadId) > 1){
                return true;
            }
            boolean result = redisOperator.setNx(resource, threadId, expireTime);
            if (result) {
                // 设置可重入
                redisOperator.reentryMap().put(resource.concat(threadId), new AtomicInteger(1));
                return result;
            }
            currentTime = System.currentTimeMillis();
        }

        throw new LockException("lock time out. resource:" + resource + ", threadId:" + threadId);
    }

    /**
     * 阻塞获取锁
     *
     * @param resource   资源ID
     * @param threadId   线程ID
     * @param waitTime   等待时间 毫秒
     * @param expireTime 过期时间 毫秒
     * @return
     */
    public boolean lock(String resource, String threadId, Long waitTime, Long expireTime) {
        if (StringUtils.isBlank(resource) || numberLessAndEqualsThanZero(waitTime) || numberLessAndEqualsThanZero(expireTime)) {
            throw new LockException("unlock param is error. resource can not be blank & waitTime and expireTime must greater zero.");
        }

        threadId = buildThreadId(threadId);
        long currentTime = System.currentTimeMillis();
        long outTime = currentTime + waitTime;
        while (currentTime < outTime) {
            boolean result = redisOperator.setNx(resource, threadId, expireTime);
            if (result) {
                return result;
            }
            currentTime = System.currentTimeMillis();
        }

        throw new LockException("lock time out. resource:" + resource + ", threadId:" + threadId);
    }

    /**
     * 释放锁
     * 由于删除不是原子操作，所以理论上存在误删的可能，当threadId可以保证全局唯一性的时候可以避免误删问题
     *
     * @param resource
     * @param threadId
     * @return
     */
    public boolean unlock(String resource, String threadId) throws LockException {
        if (StringUtils.isBlank(resource)) {
            throw new LockException("unlock param is error. resource can not be blank");
        }

        threadId = buildThreadId(threadId);

        // 如果是重入的锁直接释放
        if (reentryUnlock(resource, threadId) > 0){
            return true;
        }

        String value = redisOperator.get(resource);
        if (StringUtils.isBlank(value)) {
            return true;
        }

        if (threadId.equals(value)) {
            redisOperator.del(resource);
            return true;
        }
        throw new LockException("can not unlock other thread lock.");
    }

    /**
     * 尝试重入
     * @param resource
     * @param threadId
     * @return
     */
    private int reentryLock(String resource, String threadId){
        String reentryKey = resource.concat(threadId);
        // 看是不是重入了，如果重入了直接返回获取锁成功
        AtomicInteger atomicInteger = redisOperator.reentryMap().getOrDefault(reentryKey, DEFAULT_ZERO);
        return atomicInteger.incrementAndGet();
    }

    /**
     * 尝试重入
     * @param resource
     * @param threadId
     * @return 返回值小于等于0则可直接释放了
     */
    private int reentryUnlock(String resource, String threadId){
        String reentryKey = resource.concat(threadId);
        AtomicInteger atomicInteger = redisOperator.reentryMap().get(reentryKey);
        if (atomicInteger == null){
            return -1;
        }

        int result = atomicInteger.decrementAndGet();
        if (result == 0){
            // 防止内存泄漏
            redisOperator.reentryMap().remove(reentryKey);
        }
        return result;
    }

    /**
     * 处理线程ID
     *
     * @param threadId
     * @return
     */
    private String buildThreadId(String threadId) {
        if (StringUtils.isBlank(threadId)) {
            return NONE;
        }
        return threadId;
    }

    /**
     * 大于0返回true
     *
     * @param number
     * @return
     */
    private boolean numberLessAndEqualsThanZero(Long number) {
        return number == null || number <= 0;
    }

}
