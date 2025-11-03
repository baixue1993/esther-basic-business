package com.esther.idempotent.constant;

import lombok.Getter;

@Getter
public enum ProcessStatus {

    //待处理
    PENDING("pending", "待处理"),
    //处理中
    PROCESSING("processing", "处理中"),
    //已处理
    PROCESSED("processed", "已处理")
    ;

    private final String code;
    private final String value;
    ProcessStatus(String code, String value) {
        this.code = code;
        this.value = value;
    }
    
    public String getCode() {
        return code;
    }
}