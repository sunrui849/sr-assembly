package com.sr.assembly.lock.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 配置信息
 * @author: sunrui
 * @create: 2021/6/23 11:21 上午
 **/
@Data
@Configuration
public class RedisProperties {
    @Value("${com.sr.assembly.lock.redis.keyPrefix:redis:lock:}")
    private String keyPrefix;
}
