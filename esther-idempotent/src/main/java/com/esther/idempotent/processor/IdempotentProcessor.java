package com.esther.idempotent.processor;

import com.esther.base.common.result.Result;
import com.esther.idempotent.constant.IdempotentType;

import java.util.concurrent.TimeUnit;

public interface IdempotentProcessor {

    /**
     * 检查并获取幂等Token
     */
    Result<String> checkAndGetToken(TimeUnit timeUnit, long expire);

    /**
     * 执行幂等性检查，返回true表示需要处理，false表示不需要处理
     */
    boolean checkIdempotent(String key,  TimeUnit timeUnit, long expire, long businessExeExpire);

    /**
     * 标记为已处理
     */
    void markAsProcessed(String key, Object result, TimeUnit timeUnit, long expire);

    /**
     * 获取处理结果
     */
    Object getProcessResult(String key);

    IdempotentType getType();
}
