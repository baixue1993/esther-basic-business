package com.esther.base.common.exception;

import com.esther.base.common.result.IErrorCode;

public class BizException extends GlobalException{
    public BizException(Integer code, String message) {
        super(code, message);
    }

    public BizException(IErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage());
    }
}
