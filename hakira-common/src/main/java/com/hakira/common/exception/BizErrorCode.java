package com.hakira.common.exception;

import lombok.Getter;

/**
 * 业务错误码枚举
 * <p>
 * 统一业务错误码规范，用于 GlobalExceptionHandler 返回标准的 Result 结构。
 * 错误码分段：
 * 1000-1999 业务校验错误
 * 88888     系统异常（未知错误）
 * 99999     系统繁忙（限流/降级）
 *
 * @author hakiraKafka
 */
@Getter
public enum BizErrorCode {

    /** 通用业务处理失败 */
    BIZ_ERROR("1000", "业务处理失败"),

    /** 借贷不平衡 */
    ENTRY_UNBALANCED("1001", "借贷不平衡"),

    /** 库存不足 */
    STOCK_INSUFFICIENT("1002", "库存不足"),

    /** 分录不存在 */
    ENTRY_NOT_FOUND("1003", "分录不存在"),

    /** 会计科目不存在 */
    ACCOUNT_NOT_FOUND("1004", "会计科目不存在"),

    /** 数据已被修改（乐观锁冲突） */
    DATA_VERSION_CONFLICT("1005", "数据已被修改，请刷新后重试"),

    /** 辅助核算维度不存在 */
    AUX_DIMENSION_NOT_FOUND("1006", "辅助核算维度不存在"),

    /** 辅助核算维度值不存在 */
    AUX_VALUE_NOT_FOUND("1007", "辅助核算维度值不存在"),

    /** 凭证状态不允许当前操作 */
    ENTRY_STATUS_INVALID("1008", "凭证状态不允许当前操作"),

    /** 系统未知异常 */
    SYSTEM_ERROR("88888", "系统异常，请联系管理员"),

    /** 系统繁忙（限流/降级） */
    SYSTEM_BUSY("99999", "系统繁忙，请稍后再试");

    private final String code;
    private final String info;

    BizErrorCode(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
