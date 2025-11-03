package com.esther.idempotent.keyresolver;

import com.esther.idempotent.annotation.Idempotent;
import org.aspectj.lang.JoinPoint;

public interface IdempotentKeyResolver {

    String resolver(JoinPoint joinPoint, Idempotent idempotent);

    Boolean canResolver(Idempotent idempotent);
}
