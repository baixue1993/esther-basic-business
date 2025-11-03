package com.esther.idempotent.provider;

import com.esther.idempotent.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;

public interface IdempotentResultProvider {

    /**
     * 是否支持当前请求
     */
    Boolean supports(JoinPoint joinPoint, Idempotent idempotent, String key);


    /**
     * 获取幂等处理的结果
     */
    Object providerResult(JoinPoint joinPoint, Idempotent idempotent, String key);
}
