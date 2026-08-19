package com.hakira.ledger.api.dto.report;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账簿明细行（明细账/日记账/总账明细共用）
 */
@Data
public class LedgerEntryItem {
    private String voucherNo;
    private String entryDate;
    private String description;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
}
