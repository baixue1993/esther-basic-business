package com.esther.idempotent.exception;

import com.esther.base.common.exception.BizException;
import com.esther.base.common.result.IErrorCode;

public enum IdempotentError {

    // 8代表组件，01代表是幂等组件

    // 幂等Key解析异常
    KEY_PARSE_ERROR(80101, "幂等Key解析异常"),

    // 请求重复
    REQUEST_REPEAT(80102, "请求重复"),

    // 类型转换失败
    CONVERT_ERROR(80103, "类型转换失败"),
    ;

    private final int code;

    private final String message;


    IdempotentError(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public Integer getCode() {
        return this.code;
    }


    public String getMessage() {
        return this.message;
    }

    public IdempotentException exception() {
        return new IdempotentException(this.getCode(), this.getMessage());
    }


    public IdempotentException exception(String message) {
        return new IdempotentException(this.getCode(), message);
    }
}
