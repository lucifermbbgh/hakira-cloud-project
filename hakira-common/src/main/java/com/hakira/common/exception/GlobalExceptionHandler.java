package com.hakira.common.exception;

import com.hakira.common.pojo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 统一处理 Controller 层抛出的异常，转换为规范的 Result 结构返回（HTTP 200 + 业务错误码），
 * 避免业务错误暴露为 HTTP 500 堆栈。由各服务的 @ComponentScan("com.hakira.common") 加载。
 *
 * @author hakiraKafka
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 业务异常：返回对应业务错误码 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: code={}, info={}", e.getErrorCode(), e.getMessage());
        return Result.returnFail(e.getErrorCode(), e.getMessage());
    }

    /** 参数/校验异常（借贷不平衡、库存不足等遗留 IllegalArgumentException 兼容） */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("校验异常: {}", e.getMessage());
        return Result.returnFail(BizErrorCode.BIZ_ERROR.getCode(), e.getMessage());
    }

    /** 兜底异常：返回未知错误码，不暴露堆栈 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.returnUnknown(BizErrorCode.SYSTEM_ERROR.getCode(), BizErrorCode.SYSTEM_ERROR.getInfo());
    }
}
