package com.esther.idempotent.annotation;

import com.esther.idempotent.constant.IdempotentType;
import com.esther.idempotent.constant.RepeatStrategy;
import com.esther.idempotent.constant.TypeAdaptStrategy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解，作用在方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等键 SPEL表达式，默认空字符串，表示使用方法参数作为幂等键
     */
    String key() default "";

    /**
     * 幂等过期时间，默认单位秒，默认5秒
     */
    long expire() default 5L;

    /**
     * 业务执行时长, 默认5s
     */
    long businessExeExpire() default 5L;

    /**
     * 幂等过期时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 幂等类型 默认和TOKEN
     */
    IdempotentType type() default IdempotentType.DEFAULT;

    /**
     * 重复请求处理策略，默认DEFAULT
     */
    RepeatStrategy repeatStrategy() default RepeatStrategy.DEFAULT;

    /**
     * 自定义响应结果
     */
    String customResponse() default "";

    /**
     * 自定义响应结果适配策略
     */
    TypeAdaptStrategy typeAdaptStrategy() default TypeAdaptStrategy.AUTO;
}
