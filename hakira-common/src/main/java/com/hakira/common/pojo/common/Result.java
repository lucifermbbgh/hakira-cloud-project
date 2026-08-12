package com.hakira.common.pojo.common;

import com.hakira.common.base.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> extends BaseDto {
    //泛型类型
    //E - Element (在集合中使用，因为集合中存放的是元素)
    //T - Type（Java 类）
    //K - Key（键）
    //V - Value（值）
    //N - Number（数值类型）

    public static final Integer SUCCESS = 0;// 服务调用成功
    public static final Integer FAIL = 1;// 服务调用失败
    public static final Integer UNKNOWN = -1;// 服务调用发生未知错误

    private Integer resultCode;// 服务调用状态
    private String errorCode;// 错误码
    private String errorInfo;// 错误信息
    private T data;// 返回结果集

    public static <E> Result<E> returnSuccess(E data) {
        Result result = new Result();
        result.setResultCode(SUCCESS);
        result.setData(data);
        return result;
    }

    public static <E> Result<E> returnFail(String errorCode, String errorInfo, E data) {
        Result result = new Result();
        result.setResultCode(FAIL);
        result.setErrorCode(errorCode);
        result.setErrorInfo(errorInfo);
        result.setData(data);
        return result;
    }

    public static <E> Result<E> returnUnknown(String errorCode, String errorInfo, E data) {
        Result result = new Result();
        result.setResultCode(UNKNOWN);
        result.setErrorCode(errorCode);
        result.setErrorInfo(errorInfo);
        result.setData(data);
        return result;
    }

    public static Result returnFail(String errorCode, String errorInfo) {
        Result result = new Result();
        result.setResultCode(FAIL);
        result.setErrorCode(errorCode);
        result.setErrorInfo(errorInfo);
        return result;
    }

    public static Result returnUnknown(String errorCode, String errorInfo) {
        Result result = new Result();
        result.setResultCode(UNKNOWN);
        result.setErrorCode(errorCode);
        result.setErrorInfo(errorInfo);
        return result;
    }
}
