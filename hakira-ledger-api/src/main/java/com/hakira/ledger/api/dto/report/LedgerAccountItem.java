package com.hakira.ledger.api.dto.report;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 总账账户（科目 + 期初/本期/期末余额 + 本期明细）
 */
@Data
public class LedgerAccountItem {
    private String subjectCode;
    private String subjectName;
    private BigDecimal openingDebit;
    private BigDecimal openingCredit;
    private BigDecimal periodDebit;
    private BigDecimal periodCredit;
    private BigDecimal closingDebit;
    private BigDecimal closingCredit;
    /** 本期明细行 */
    private List<LedgerEntryItem> entries;
}
