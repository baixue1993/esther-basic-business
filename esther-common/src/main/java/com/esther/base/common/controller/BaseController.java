package com.esther.base.common.controller;

import com.esther.base.common.result.Result;

public class BaseController {

    public <T> Result<T> success(T data) {
        return Result.success(data);
    }
}
