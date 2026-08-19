package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会计分录头实体（对应 journal_entry 表，按月分区）
 *
 * @author hakiraKafka
 */
@Data
public class JournalEntry {
    /** 分录ID（20位雪花ID） */
    private String entryId;
    /** 记账日期（分区键） */
    private LocalDate entryDate;
    /** 凭证号 */
    private String voucherNo;
    /** 摘要 */
    private String description;
    /** 借方合计 */
    private BigDecimal totalDebit;
    /** 贷方合计 */
    private BigDecimal totalCredit;
    /** 状态：POSTED=已入账 REVERSED=已冲销 */
    private String status;
    /** 乐观锁版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
