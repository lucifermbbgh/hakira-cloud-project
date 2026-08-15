package com.hakira.ledger.api.dto.aging;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 坏账核销/收回请求
 */
@Data
public class BadDebtRequest {
    /** 客户编码（CUSTOMER 维度值） */
    private String customerCode;
    /** 金额 */
    private BigDecimal amount;
}
