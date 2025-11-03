package com.esther.base.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {

    public static final int SUCCESS_CODE = 200;

    public static final int ERROR_CODE = 500;
    public static final String SUCCESS_MSG = "操作成功";

    private int code;

    private String message;

    private Boolean success;

    private T data;

    public static Result<String> success() {
        return ok();
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<T>();
        r.code = code;
        r.message = message;
        r.success = false;
        return r;
    }

    public static<T> Result<T> success(T data) {
        return ok("操作成功", data);
    }

    public static <T> Result<T> ok() {
        return ok(SUCCESS_MSG, null);
    }

    public static <T> Result<T> ok(String message, T data) {
        Result<T> r = new Result();
        r.success = true;
        r.message = message;
        r.code = SUCCESS_CODE;
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(String message, T data) {
        Result<T> r = new Result<T>();
        r.success = false;
        r.code = ERROR_CODE;
        r.message = message;
        r.data = data;
        return r;
    }
}
