package com.esther.idempotent.keyresolver.impl;

import com.esther.idempotent.annotation.Idempotent;
import com.esther.idempotent.keyresolver.IdempotentKeyResolver;
import com.esther.idempotent.utils.SpelHelper;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ExpressionIdempotentKeyResolver implements IdempotentKeyResolver {

    @Autowired
    private SpelHelper spelHelper;

    @Override
    public String resolver(JoinPoint joinPoint, Idempotent idempotent) {
        return this.spelHelper.parseString(joinPoint, idempotent.key());
    }

    @Override
    public Boolean canResolver(Idempotent idempotent) {
        return !StringUtils.hasText(idempotent.key());
    }
}
