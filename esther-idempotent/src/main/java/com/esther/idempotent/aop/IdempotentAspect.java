package com.esther.idempotent.aop;


import com.alibaba.fastjson.JSONObject;
import com.esther.base.common.result.Result;
import com.esther.idempotent.annotation.Idempotent;
import com.esther.idempotent.exception.IdempotentError;
import com.esther.idempotent.keyresolver.IdempotentKeyResolver;
import com.esther.idempotent.processor.IdempotentProcessor;
import com.esther.idempotent.processor.IdempotentProcessorFactory;
import com.esther.idempotent.provider.IdempotentResultProvider;
import com.esther.idempotent.utils.SpelHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class IdempotentAspect {

    private final IdempotentProcessorFactory idempotentProcessorFactory;

    private final SpelHelper spelHelper;

    private final HttpServletResponse response;

    private final List<IdempotentResultProvider> resultProviders;

    private final List<IdempotentKeyResolver> keyResolvers;

    public IdempotentAspect(IdempotentProcessorFactory idempotentProcessorFactory,
                            SpelHelper spelHelper,
                            HttpServletResponse response,
                            List<IdempotentResultProvider> resultProviders,
                            List<IdempotentKeyResolver> keyResolvers) {
        this.idempotentProcessorFactory = idempotentProcessorFactory;
        this.spelHelper = spelHelper;
        this.response = response;
        this.resultProviders = resultProviders;
        this.keyResolvers = keyResolvers;
    }

    @Around("@annotation(idempotent)")
    public Object handleIdempotent(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        //获取幂等键
        String key = parseKey(joinPoint,idempotent);
        //判断幂等类型
        IdempotentProcessor idempotentProcessor = idempotentProcessorFactory.getIdempotentProcessor(idempotent.type());
        if(idempotentProcessor.checkIdempotent(key, idempotent.timeUnit(), idempotent.expire(), idempotent.businessExeExpire())) {
            //TODO 这里可能并不需要存储数据，因为不涉及返回
            //TODO 业务数据保存多少时间需要确认，无论是是否有业务数据的返回，都需要进行业务的操作
            //TODO 无法根据不同的流程做处理，例如没有token,没有业务数据（可能正在处理）,有业务数据
            Object result = joinPoint.proceed() ;
            /*Object saveData = idempotent.strategy().equals(RepeatStrategy.RETURN_VALUE) ?
                    result : ProcessStatus.PROCESSED.getCode();*/
            idempotentProcessor.markAsProcessed(key, result, idempotent.timeUnit(), idempotent.expire());
            return result;
        } else {
            return switch (idempotent.repeatStrategy()) {
                case CUSTOM_RESPONSE -> handelResult(joinPoint, idempotent,key);
                case IGNORE -> joinPoint.proceed();
                case RETURN_VALUE -> idempotentProcessor.getProcessResult(key);
                case THROW_EXCEPTION -> throw IdempotentError.REQUEST_REPEAT.exception();
                default -> Result.success(null);
            };
        }
    }

    /**
     * 解析自定义的响应结果
     */
    private Object evaluateCustomResult(JoinPoint joinPoint, Idempotent idempotent, String key) {
        return spelHelper.parseObject(joinPoint, idempotent.customResponse());
    }

    private Object handelResult(JoinPoint joinPoint, Idempotent idempotent, String key) {
        Object customResult = evaluateCustomResult(joinPoint, idempotent, key);
        //方法上有自定义的响应用自定义的
        if (customResult != null) {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            Class<?> returnType = signature.getReturnType();
            switch (idempotent.typeAdaptStrategy()) {
                case AUTO -> {
                    return autoAdapt(customResult, returnType);
                }
                case WRAP -> {
                    return wrapToDeclaredType(customResult, returnType);
                }
                case FORCE -> {
                    return forceType(customResult, returnType);
                }
                case COMPATIBLE -> {
                    return compatibleAdapt(customResult, returnType);
                }
                default -> {
                    return autoAdapt(customResult, returnType);
                }
            }
        }
        //方法上没有自定义但是自定义了解析器用自定义的
        for (IdempotentResultProvider resultProvider : resultProviders) {
            if (resultProvider.supports(joinPoint, idempotent, key)) {
                return resultProvider.providerResult(joinPoint, idempotent, key);
            }
        }
        //啥也没有默认返回
        return Result.success(null);
    }

    private Object compatibleAdapt(Object customResult, Class<?> declaredType) {
        // 对于Web返回类型特殊处理
        if (isWebReturnType(declaredType)) {
            return handleWebReturnType(customResult, declaredType);
        }
        return autoAdapt(customResult, declaredType);
    }

    private Object handleWebReturnType(Object customResult, Class<?> declaredType) {
        if (declaredType == ResponseEntity.class) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(customResult);
        }

        if (declaredType == String.class) {
            // 如果是字符串类型，可能是视图名，但我们返回JSON
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            return objectToJson(customResult);
        }

        if (declaredType == void.class) {
            // void方法，直接写入响应
            writeToResponse(customResult);
            return null;
        }

        return customResult;
    }

    private void writeToResponse(Object result) {
        try {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getWriter(), result);
            response.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("写入响应失败", e);
        }
    }

    private String objectToJson(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return JSONObject.toJSONString(Result.error("序列化失败", null));
        }
    }

    private boolean isWebReturnType(Class<?> declaredType) {
        return declaredType == ResponseEntity.class ||
                declaredType == ModelAndView.class ||
                declaredType == String.class || // 视图名
                declaredType == void.class ||
                declaredType == Map.class ||
                declaredType == Model.class;

    }

    private Object forceType(Object customResult, Class<?> declaredType) {
        writeToResponse(customResult);
        return null;
    }

    private Object wrapToDeclaredType(Object customResult, Class<?> declaredType) {
        // 如果声明类型是Result，尝试包装
        if (declaredType == Result.class) {
            return Result.success(customResult);
        }


        // 如果声明类型是ResponseEntity，包装
        if (declaredType == ResponseEntity.class) {
            return ResponseEntity.ok(customResult);
        }

        // 其他情况回退到自动适配
        return autoAdapt(customResult, declaredType);
    }

    private Object autoAdapt(Object customResult, Class<?> declaredType) {
        // 如果类型匹配，直接返回
        if (declaredType.isInstance(customResult)) {
            return customResult;
        }

        // 如果是Object类型，直接返回
        if (declaredType == Object.class) {
            return customResult;
        }

        // 尝试使用Spring转换
        try {
            ConversionService conversionService = DefaultConversionService.getSharedInstance();
            if (conversionService.canConvert(customResult.getClass(), declaredType)) {
                return conversionService.convert(customResult, declaredType);
            }
        } catch (Exception e) {
            log.error("无法使用Spring转换器进行类型转换:{}", e.getMessage());
        }
        //使用普通转换器
        return handleCommonTypes(customResult, declaredType);
    }

    private Object handleCommonTypes(Object customResult, Class<?> declaredType) {
        //Map->Object 序列化
        if (customResult instanceof Map && declaredType != Object.class) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(customResult);
                return mapper.readValue(json, declaredType);
            } catch (Exception e) {
                log.error("Map转Object失败:{}", e.getMessage());
                throw IdempotentError.CONVERT_ERROR.exception();
            }
        }

        //String 转其他类型
        if (customResult instanceof String && declaredType != String.class) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue((String) customResult, declaredType);
            } catch (Exception e) {
                log.error("字符串转对象失败:{}", e.getMessage());
                throw IdempotentError.CONVERT_ERROR.exception();
            }
        }

        //尝试构造方法转换
        return tryConstructorAdapt(customResult, declaredType);
    }

    private Object tryConstructorAdapt(Object customResult, Class<?> declaredType) {
        try {
            Constructor<?>[] constructors = declaredType.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == 1 && paramTypes[0].isInstance(customResult)) {
                    return constructor.newInstance(customResult);
                }
            }
        } catch (Exception e) {
            log.error("无法使用构造方法进行类型转换:{}", e.getMessage());
        }
        log.warn("无法将类型 {} 适配到声明类型 {}, 直接返回自定义结果",
                customResult.getClass().getSimpleName(), declaredType.getSimpleName());
        return customResult;
    }

    private String parseKey(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        for (IdempotentKeyResolver keyResolver : keyResolvers) {
            if (keyResolver.canResolver(idempotent)) {
                return keyResolver.resolver(joinPoint, idempotent);
            }
        }
        return null;
    }
}
