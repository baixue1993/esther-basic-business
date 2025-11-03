package com.esther.base.common.result;

import com.esther.base.common.exception.BizException;

public interface IErrorCode {

    Integer getCode();

    String getMessage();

    BizException exception();

    BizException exception(String message);
}
