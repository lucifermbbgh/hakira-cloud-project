package com.hakira.ledger.api.dto.closing;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 科目余额行（期初 / 本期发生 / 期末）
 */
@Data
public class AccountBalanceResponse {
    /** 科目编码 */
    private String subjectCode;
    /** 科目名称 */
    private String subjectName;
    /** 期初借方余额 */
    private BigDecimal openingDebit;
    /** 期初贷方余额 */
    private BigDecimal openingCredit;
    /** 本期借方发生额 */
    private BigDecimal periodDebit;
    /** 本期贷方发生额 */
    private BigDecimal periodCredit;
    /** 期末借方余额 */
    private BigDecimal closingDebit;
    /** 期末贷方余额 */
    private BigDecimal closingCredit;
}
