package com.esther.idempotent.constant;

/**
 * 幂等类型
 */
public enum IdempotentType {

    TOKEN,      //Token方式做幂等
    DEFAULT     //默认以方法名称和参数以MD5方式压缩做幂等
}
