package com.hakira.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceFailException extends Exception {
    public static final String DEFAULT_ERROR_CODE = "10000001";
    public static final String DEFAULT_ERROR_INFO = "服务处理失败！";

    private String errorCode;
    private String errorInfo;
}
