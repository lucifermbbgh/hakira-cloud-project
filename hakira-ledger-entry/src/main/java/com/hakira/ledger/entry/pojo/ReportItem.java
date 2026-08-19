package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 报表聚合条目（跨 account_balance / journal_entry_line_aux 的查询结果）
 *
 * @author hakiraKafka
 */
@Data
public class ReportItem {
    /** 科目编码 */
    private String subjectCode;
    /** 科目名称 */
    private String subjectName;
    /** 科目类别 */
    private String category;
    /** 余额方向 */
    private String balanceDirection;
    /** 期末借方余额 */
    private BigDecimal closingDebit;
    /** 期末贷方余额 */
    private BigDecimal closingCredit;
    /** 本期借方发生额 */
    private BigDecimal periodDebit;
    /** 本期贷方发生额 */
    private BigDecimal periodCredit;
    /** 现金流量维度值（现金流量表用） */
    private String valueCode;
}
