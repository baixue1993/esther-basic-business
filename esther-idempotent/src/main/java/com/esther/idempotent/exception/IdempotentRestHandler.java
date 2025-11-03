package com.esther.idempotent.exception;

import com.esther.base.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class IdempotentRestHandler {

    @ExceptionHandler(IdempotentException.class)
    public Result<String> handleIdempotentException(IdempotentException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
}
