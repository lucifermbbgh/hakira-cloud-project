package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账簿查询行（明细账/日记账/总账明细）
 *
 * @author hakiraKafka
 */
@Data
public class LedgerRow {
    /** 科目编码（总账明细分组用） */
    private String subjectCode;
    /** 凭证号 */
    private String voucherNo;
    /** 记账日期 */
    private LocalDate entryDate;
    /** 摘要 */
    private String description;
    /** 借方金额 */
    private BigDecimal debitAmount;
    /** 贷方金额 */
    private BigDecimal creditAmount;
}
