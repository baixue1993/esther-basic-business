package com.esther.idempotent.processor.impl;

import com.esther.base.common.result.Result;
import com.esther.idempotent.config.IdempotentProperties;
import com.esther.idempotent.constant.IdempotentType;
import com.esther.idempotent.processor.IdempotentProcessor;
import com.esther.idempotent.store.IdempotentStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
public class UniqueKeyIdempotentProcessor implements IdempotentProcessor  {

    private final IdempotentStorage idempotentStorage;
    private final IdempotentProperties idempotentProperties;
    
    @Autowired
    public UniqueKeyIdempotentProcessor(IdempotentStorage idempotentStorage, IdempotentProperties idempotentProperties) {
        this.idempotentStorage = idempotentStorage;
        this.idempotentProperties = idempotentProperties;
    }

    @Override
    public Result<String> checkAndGetToken(TimeUnit timeUnit, long expire) {
        throw new UnsupportedOperationException("不支持此方法");
    }

    @Override
    public boolean checkIdempotent(String key, TimeUnit timeUnit, long expire, long businessExeExpire) {
        String uniqueKey = idempotentProperties.getTokenPrefix() + key;
        String processedKey = idempotentProperties.getProcessedPrefix() + key;
        if (idempotentStorage.exists(processedKey)) {
            return false;
        }
        return idempotentStorage.storeToken(uniqueKey, TimeUnit.SECONDS, businessExeExpire);
    }

    @Override
    public void markAsProcessed(String key, Object result, TimeUnit timeUnit, long expireTime) {
        String uniqueKey = idempotentProperties.getTokenPrefix() + key;
        String processedKey = idempotentProperties.getProcessedPrefix() + key;
        //标记为已处理，存储处理结果，可以设置一个合理的过期时间
        idempotentStorage.storeProcessed(processedKey, result, timeUnit, expireTime);

        //删除锁
        idempotentStorage.delete(uniqueKey);
    }

    @Override
    public Object getProcessResult(String uniqueKey) {
        String processedKey = idempotentProperties.getProcessedPrefix() + uniqueKey;
        return idempotentStorage.getProcessedResult(processedKey);
    }

    @Override
    public IdempotentType getType() {
        return IdempotentType.DEFAULT;
    }
}
