package com.esther.idempotent.keyresolver.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.esther.idempotent.annotation.Idempotent;
import com.esther.idempotent.keyresolver.IdempotentKeyResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 默认幂等Key解析器，使用方法和参数以MD5取值作为Key
 */

@Component
public class DefaultIdempotentKeyResolver implements IdempotentKeyResolver {
    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        String methodName = joinPoint.getSignature().toString();
        String params = StrUtil.join(",", joinPoint.getArgs());
        return SecureUtil.md5(methodName + params);
    }

    @Override
    public Boolean canResolver(Idempotent idempotent) {
        return !StringUtils.hasText(idempotent.key());
    }
}
