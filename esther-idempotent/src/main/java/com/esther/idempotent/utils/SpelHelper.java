package com.esther.idempotent.utils;

import cn.hutool.core.util.ArrayUtil;
import com.esther.idempotent.exception.IdempotentError;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

public class SpelHelper {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();


    public EvaluationContext createContext(JoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = this.parameterNameDiscoverer.getParameterNames(method);
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (ArrayUtil.isNotEmpty(parameterNames)) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        return context;
    }

    public String parseString(JoinPoint joinPoint, String key) {
        Expression expression = this.parser.parseExpression(key);
        return expression.getValue(createContext(joinPoint), String.class);
    }

    public Object parseObject(JoinPoint joinPoint, String result) {
        Expression expression = this.parser.parseExpression(result);
        return expression.getValue(createContext(joinPoint));
    }


    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        //处理声明在类上的情况
        if (!method.getDeclaringClass().isInterface()) {
            return method;
        }
        //处理声明在接口上的情况
        try {
            return joinPoint.getClass().getDeclaredMethod(joinPoint.getSignature().getName(),
                    method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw IdempotentError.KEY_PARSE_ERROR.exception();
        }
    }
}
