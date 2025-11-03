package com.esther.idempotent.store;

import java.util.concurrent.TimeUnit;

public interface IdempotentStorage {

    /**
     * 存储幂等Token
     */
    boolean storeToken(String tokenKey, TimeUnit timeUnit, long expire);

    boolean exists(String tokenKey);

    boolean compareAndSet(String tokenKey, long expire);

    void storeProcessed(String processedKey, Object result, TimeUnit timeUnit, long expireTime);

    Object getProcessedResult(String processedKey);

    void delete(String uniqueKey);
}
