package com.esther.base.common.exception;

import lombok.Data;

@Data
public class GlobalException extends RuntimeException {

    private Integer code;

    private String message;

    public GlobalException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
