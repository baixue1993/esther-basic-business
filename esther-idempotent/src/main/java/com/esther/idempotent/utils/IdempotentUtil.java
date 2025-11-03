package com.esther.idempotent.utils;

import java.util.UUID;

public class IdempotentUtil {

    /**
     * 使用UUID 生成唯一Token
     */
    public static String generateToken() {
        //TODO 可以考虑使用更复杂的算法生成Token
        return UUID.randomUUID().toString();
    }
}
