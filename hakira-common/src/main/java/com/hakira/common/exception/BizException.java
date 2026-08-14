package com.hakira.common.exception;

import lombok.Getter;

/**
 * 业务异常（运行时异常）
 * <p>
 * 携带业务错误码与错误信息，由 GlobalExceptionHandler 统一捕获并转换为 Result 结构。
 * 相比旧的 ServiceFailException（checked exception extends Exception），
 * 改为继承 RuntimeException，符合 Spring 事务回滚与声明式异常处理惯例。
 *
 * @author hakiraKafka
 */
@Getter
public class BizException extends RuntimeException {

    private final String errorCode;

    public BizException(String errorCode, String errorInfo) {
        super(errorInfo);
        this.errorCode = errorCode;
    }

    public BizException(BizErrorCode errorCode) {
        super(errorCode.getInfo());
        this.errorCode = errorCode.getCode();
    }

    public BizException(BizErrorCode errorCode, String detail) {
        super(errorCode.getInfo() + "：" + detail);
        this.errorCode = errorCode.getCode();
    }
}
