package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 科目余额实体（对应 account_balance 表，结账时物化）
 *
 * @author hakiraKafka
 */
@Data
public class AccountBalance {
    /** 会计期间 YYYYMM */
    private String period;
    /** 科目编码 */
    private String subjectCode;
    /** 科目名称 */
    private String subjectName;
    /** 余额方向（聚合查询临时字段，非表字段） */
    private String balanceDirection;
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
    /** 乐观锁版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
