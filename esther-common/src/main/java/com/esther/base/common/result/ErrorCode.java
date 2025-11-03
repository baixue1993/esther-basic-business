package com.esther.base.common.result;

import com.esther.base.common.exception.BizException;
import lombok.Getter;

@Getter
public enum ErrorCode implements IErrorCode{
    SUCCESS(200, "操作成功"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源未找到"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    PARAM_ERROR(101, "参数错误"),
    FAILED(999, "操作失败"),
    ;


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public BizException exception() {
        return new BizException(this);
    }

    @Override
    public BizException exception(String message) {
        return new BizException(this.getCode(), message);
    }
}
