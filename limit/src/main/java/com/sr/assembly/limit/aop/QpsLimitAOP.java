package com.sr.assembly.limit.aop;

import com.sr.assembly.limit.LimitException;
import com.sr.assembly.limit.QpsLimitUtil;
import com.sr.assembly.limit.annotation.QpsLimit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * @author: sunrui
 * @date: 2020/7/21 11:12
 * @description: 限流AOP
 */
@Order(1)
@Aspect
@Slf4j
public class QpsLimitAOP {

    @Around("@annotation(com.sr.assembly.limit.annotation.QpsLimit)")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!(joinPoint.getSignature() instanceof MethodSignature)) {
            log.error("QpsLimitAOP joinPoint.getSignature() instanceof MethodSignature error!");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        try {
            QpsLimit qpsLimit = method.getAnnotation(QpsLimit.class);
            String group = qpsLimit.group();
            int qps = qpsLimit.qps();
            QpsLimitUtil.limit(group, qps);
        } catch (LimitException e) {
            log.error("QpsLimitAOP limit.", e);
            return null;
        } catch (Exception e) {
            log.error("QpsLimitAOP error ", e);
        }
        return joinPoint.proceed();
    }
}
