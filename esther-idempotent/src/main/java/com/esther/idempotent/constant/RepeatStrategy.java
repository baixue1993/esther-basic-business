package com.esther.idempotent.constant;

public enum RepeatStrategy {

    //返回响应的值
    RETURN_VALUE,

    //默认响应策略
    DEFAULT,

    //抛出异常
    THROW_EXCEPTION,

    //不处理
    IGNORE,

    //返回自定义的响应
    CUSTOM_RESPONSE
}
