package com.esther.idempotent.store.impl;

import com.esther.idempotent.constant.ProcessStatus;
import com.esther.idempotent.store.IdempotentStorage;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RedisIdempotentStorage implements IdempotentStorage {

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisTemplate<String, String> stringRedisTemplate;

    public RedisIdempotentStorage(RedisTemplate<String, Object> redisTemplate, RedisTemplate<String, String> stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    /**
     * 方案1：非原子性，可能存在setIfAbsent后，其他线程去设置值的问题
     * redisTemplate.opsForValue().setIfAbsent(tokenKey,1);
     * redisTemplate.expire(tokenKey, expire, timeUnit);
     * <p>
     *
     * 方案2: 存在竟态条件：如果线程A执行setIfAbsent成功，但是在设置过期时间之前被挂起，这个key可能会永久存在
     * if(redisTemplate.opsForValue().setIfAbsent("11111111","1")){
     *    redisTemplate.expire("11111111", 5, TimeUnit.MINUTES);
     * }
     * <p>
     *
     * 方案3：使用SET NX EX命令保证原子性操作
     * StringRedisSerializer serializer = new StringRedisSerializer();
     * byte[] keyBytes = serializer.serialize(tokenKey);
     * byte[] valueBytes = serializer.serialize("1");
     * Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection ->
     * connection.stringCommands().set(keyBytes, valueBytes,
     *   Expiration.from(expire, timeUnit),
     *         RedisStringCommands.SetOption.SET_IF_ABSENT)
     * );
     * <p>
     *
     * 方案4：lua脚本原子操作
     * String script =
     *     "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
     *     "   return redis.call('expire', KEYS[1], ARGV[2]) " +
     *     "else " +
     *     "   return 0 " +
     *     "end";
     * Boolean result = redisTemplate.execute(
     *     new DefaultRedisScript<>(script, Boolean.class),
     *     Collections.singletonList("11111111"),
     *     "1",
     *     String.valueOf(TimeUnit.MINUTES.toSeconds(5))
     * );
     */
    @Override
    public boolean storeToken(String tokenKey, TimeUnit timeUnit, long expire) {
        Boolean b = stringRedisTemplate.opsForValue().setIfAbsent(tokenKey, ProcessStatus.PENDING.getCode(), expire, timeUnit);

        //使用setIfAbsent的重载方法，本质上还使用的是方案3实现的
        return Boolean.TRUE.equals(b);
    }

    @Override
    public boolean exists(String tokenKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
    }

    @Override
    public boolean compareAndSet(String tokenKey, long expire) {
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "redis.call('set', KEYS[1], ARGV[2], 'EX', ARGV[3]) " +
                        "return 1 " +
                        "else " +
                        "return 0 " +
                        "end";
        Long result = stringRedisTemplate.execute((RedisCallback<Long>)connection -> {
            //GenericJackson2JsonRedisSerializer redisSerializer = (GenericJackson2JsonRedisSerializer) redisTemplate.getValueSerializer();
            return connection.scriptingCommands().eval(luaScript.getBytes(),
                    ReturnType.INTEGER,
                    1,
                    tokenKey.getBytes(),
                    ProcessStatus.PENDING.getCode().getBytes(),
                    ProcessStatus.PROCESSING.getCode().getBytes(),
                    String.valueOf(expire).getBytes());
            }

        );
        return result == 1;
    }

    @Override
    public void storeProcessed(String processedKey, Object result, TimeUnit timeUnit, long expireTime) {
        redisTemplate.opsForValue().set(processedKey, result, expireTime, timeUnit);
    }

    @Override
    public Object getProcessedResult(String processedKey) {
        return redisTemplate.opsForValue().get(processedKey);
    }

    @Override
    public void delete(String uniqueKey) {
        redisTemplate.delete(uniqueKey);
    }
}
