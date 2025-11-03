package com.esther.idempotent.processor;

import com.esther.idempotent.constant.IdempotentType;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class IdempotentProcessorFactory {

    private final Map<IdempotentType, IdempotentProcessor> idempotentProcessorMap;

    public IdempotentProcessorFactory(List<IdempotentProcessor> processors) {
        this.idempotentProcessorMap = new EnumMap<>(IdempotentType.class);
        for (IdempotentProcessor processor : processors) {
            this.idempotentProcessorMap.put(processor.getType(), processor);
        }
    }

    public IdempotentProcessor getIdempotentProcessor(IdempotentType type) {
        return idempotentProcessorMap.get(type);
    }
}
