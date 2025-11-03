package com.esther.idempotent.config;

import com.esther.idempotent.aop.IdempotentAspect;
import com.esther.idempotent.keyresolver.IdempotentKeyResolver;
import com.esther.idempotent.processor.IdempotentProcessor;
import com.esther.idempotent.processor.IdempotentProcessorFactory;
import com.esther.idempotent.provider.IdempotentResultProvider;
import com.esther.idempotent.store.IdempotentStorage;
import com.esther.idempotent.store.impl.RedisIdempotentStorage;
import com.esther.idempotent.utils.SpelHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@EnableAspectJAutoProxy
@Configuration
@ComponentScan(basePackages = "com.esther.idempotent")
public class IdempotentConfig {

    @Bean("idempotentRedisConnectionFactory")
    public RedisConnectionFactory idempotentRedisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean("idempotentRedisTemplate")
    public RedisTemplate<String, Object> idempotentRedisTemplate(
            @Qualifier("idempotentRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }

    @Bean("idempotentStringRedisTemplate")
    @Primary
    public RedisTemplate<String, String> idempotentStringRedisTemplate(
            @Qualifier("idempotentRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentStorage idempotentStorage(@Qualifier("idempotentRedisTemplate") RedisTemplate<String, Object> idempotentRedisTemplate,
                                               @Qualifier("idempotentStringRedisTemplate") RedisTemplate<String, String> idempotentStringRedisTemplate) {
        return new RedisIdempotentStorage(idempotentRedisTemplate, idempotentStringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public SpelHelper spelHelper() {
        return new SpelHelper();
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentProcessorFactory idempotentProcessorFactory(List<IdempotentProcessor> processors) {
        return new IdempotentProcessorFactory(processors);
    }

    @Bean
    public IdempotentAspect idempotentAspect(@Autowired IdempotentProcessorFactory idempotentProcessorFactory,
                                             @Autowired SpelHelper spelHelper,
                                             @Autowired List<IdempotentResultProvider> resultProviders,
                                             @Autowired HttpServletResponse response,
                                             @Autowired List<IdempotentKeyResolver> keyResolvers) {
        return new IdempotentAspect(idempotentProcessorFactory, spelHelper, response,
                resultProviders, keyResolvers);
    }
}
