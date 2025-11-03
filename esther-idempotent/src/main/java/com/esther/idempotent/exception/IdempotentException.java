package com.esther.idempotent.exception;

import com.esther.base.common.exception.GlobalException;
import com.esther.base.common.result.IErrorCode;

public class IdempotentException extends GlobalException {


    public IdempotentException(Integer code, String message) {
        super(code, message);
    }

    public IdempotentException(IErrorCode error) {
        super(error.getCode(), error.getMessage());
    }
}
