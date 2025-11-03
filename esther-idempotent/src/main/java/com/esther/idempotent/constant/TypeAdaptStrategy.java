package com.esther.idempotent.constant;

public enum TypeAdaptStrategy {

    AUTO,                      // 自动适配,根据类型适配
    WRAP,                      //  包装适配，将返回结果包装后适配
    FORCE,                     // 强制适配，不论什么按照用户自定义的返回
    COMPATIBLE                 // 兼容适配，尽量适配用户自定义的返回
    ;
}
