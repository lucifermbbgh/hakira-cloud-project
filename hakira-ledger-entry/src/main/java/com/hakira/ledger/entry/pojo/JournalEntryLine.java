package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会计分录行实体（对应 journal_entry_line 表，按月分区）
 *
 * @author hakiraKafka
 */
@Data
public class JournalEntryLine {
    /** 行ID（自增） */
    private Long lineId;
    /** 分录ID */
    private String entryId;
    /** 记账日期（分区键，冗余自主表） */
    private LocalDate entryDate;
    /** 行号 */
    private Integer lineNo;
    /** 科目编码（引用 account_subject） */
    private String subjectCode;
    /** 科目名称（快照冗余） */
    private String subjectName;
    /** 摘要 */
    private String description;
    /** 借方金额 */
    private BigDecimal debitAmount;
    /** 贷方金额 */
    private BigDecimal creditAmount;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
