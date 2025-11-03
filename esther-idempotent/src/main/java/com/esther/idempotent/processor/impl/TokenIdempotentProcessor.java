package com.esther.idempotent.processor.impl;

import com.esther.base.common.result.Result;
import com.esther.idempotent.config.IdempotentProperties;
import com.esther.idempotent.constant.IdempotentType;
import com.esther.idempotent.exception.IdempotentError;
import com.esther.idempotent.processor.IdempotentProcessor;
import com.esther.idempotent.store.IdempotentStorage;
import com.esther.idempotent.utils.IdempotentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenIdempotentProcessor implements IdempotentProcessor {

    private final IdempotentStorage idempotentStorage;
    private final IdempotentProperties idempotentProperties;

    public TokenIdempotentProcessor(IdempotentStorage idempotentStorage, IdempotentProperties idempotentProperties) {
        this.idempotentStorage = idempotentStorage;
        this.idempotentProperties = idempotentProperties;
    }

    @Override
    public Result<String> checkAndGetToken(TimeUnit timeUnit, long expire) {
        String token = IdempotentUtil.generateToken();
        String tokenKey = idempotentProperties.getTokenPrefix() + token;

        //存储Key，有效期为传入的有效期
        boolean success = idempotentStorage.storeToken(tokenKey, timeUnit, expire);
        if (success) {
            return Result.success(token);
        }
        return Result.error("获取Token失败", null);
    }

    @Override
    public boolean checkIdempotent(String token,  TimeUnit timeUnit, long expire, long businessExeExpire) {
        String tokenKey = idempotentProperties.getTokenPrefix() + token;
        String processedKey = idempotentProperties.getProcessedPrefix() + token;
        //检查token是否有效
        if (!idempotentStorage.exists(tokenKey)) {
            throw IdempotentError.REQUEST_REPEAT.exception();
        }
        //检查token是否已经被处理,不需要再次处理
        if(idempotentStorage.exists(processedKey)) {
            return false;
        }
        //删除token，expire必须大于业务执行的时间，为什么不是直接删除token？是因为可能存在并发请求，删除后其他请求就无法判断是幂等性的报错还是业务执行延迟的报错
        //这样做可以保证在业务短期执行的时间内，用户可以获知，可以有较好的用户体验
        //使用自定义业务执行时间，避免并发请求导致token被删除
        return idempotentStorage.compareAndSet(tokenKey, businessExeExpire);
    }

    @Override
    public void markAsProcessed(String token, Object result, TimeUnit timeUnit, long expire) {
        String processedKey = idempotentProperties.getProcessedPrefix() + token;
        //标记为已处理，存储处理结果，可以设置一个合理的过期时间
        //用户传入的幂等过期时间就是业务保存的时间
        idempotentStorage.storeProcessed(processedKey, result, timeUnit, expire);
    }

    @Override
    public Object getProcessResult(String token) {
        String processedKey = idempotentProperties.getProcessedPrefix() + token;
        return idempotentStorage.getProcessedResult(processedKey);
    }

    @Override
    public IdempotentType getType() {
        return IdempotentType.TOKEN;
    }
}
