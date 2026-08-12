package com.hakira.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUnknownException extends Exception {
    public static final String DEFAULT_ERROR_CODE = "10000002";
    public static final String DEFAULT_ERROR_INFO = "服务处理发生未知异常！";

    private String errorCode;
    private String errorInfo;
}
