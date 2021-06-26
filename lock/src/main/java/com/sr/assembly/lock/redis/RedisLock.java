package com.sr.assembly.lock.redis;

import com.sr.assembly.lock.LockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.ConcurrentHashMap;
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
        if (StringUtils.isBlank(resource) || !numberGreaterThanZero(expireTime)) {
            throw new LockException("unlock param is error. resource can not be blank & expireTime must greater zero.");
        }

        threadId = buildThreadId(threadId);
        return redisOperator.setNx(resource, threadId, expireTime);
    }

    // todo   1。 使用threadLocal实现，如果值用value实现那么不能支持同线程多个不同的锁，如果用hashmap，那么无法在unlock的时候remove;
    /**
     * 阻塞获取可重入锁 todo delete
     *
     * @param resource   资源ID
     * @param threadId   线程ID
     * @param waitTime   等待时间 毫秒
     * @param expireTime 过期时间 毫秒 过期时间以第一个获取到锁的为准，需要业务谨慎考虑设置过期时间
     * @return
     */
    @Deprecated
    public boolean reentryLock(String resource, String threadId, Long waitTime, Long expireTime) {
        if (StringUtils.isBlank(resource) || !numberGreaterThanZero(waitTime) || !numberGreaterThanZero(expireTime)) {
            throw new LockException("unlock param is error. resource can not be blank & waitTime and expireTime must greater zero.");
        }

        threadId = buildThreadId(threadId);

        // 看是不是重入了，如果重入了直接返回获取锁成功
        ConcurrentHashMap<String, AtomicInteger> threadMap = redisOperator.threadLocal().get();
        if (!CollectionUtils.isEmpty(threadMap)) {
            AtomicInteger atomicInteger = currentLockers().getOrDefault(resource.concat(threadId), DEFAULT_ZERO);
            if (atomicInteger.intValue() > 0) {
                // 如果大于0说明在锁着，获取到可重入锁
                atomicInteger.incrementAndGet();
                return true;
            }
        }

        long currentTime = System.currentTimeMillis();
        long outTime = currentTime + waitTime;
        while (currentTime < outTime) {
            boolean result = redisOperator.setNx(resource, threadId, expireTime);
            if (result) {
                // 设置可重入
                currentLockers().put(resource.concat(threadId), new AtomicInteger(1));
                return result;
            }
            currentTime = System.currentTimeMillis();
        }

        throw new LockException("lock time out. resource:" + resource + ", threadId:" + threadId);
    }

    /**
     * 获取重入锁 todo delete
     * @return
     */
    @Deprecated
    private synchronized ConcurrentHashMap<String, AtomicInteger> currentLockers(){
        ConcurrentHashMap<String, AtomicInteger> concurrentLockers = redisOperator.threadLocal().get();
        if (concurrentLockers == null){
            redisOperator.threadLocal().set(new ConcurrentHashMap<>());
        }
        return concurrentLockers;
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
        if (StringUtils.isBlank(resource) || !numberGreaterThanZero(waitTime) || !numberGreaterThanZero(expireTime)) {
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
    private boolean numberGreaterThanZero(Long number) {
        return number != null && number > 0;
    }

}
