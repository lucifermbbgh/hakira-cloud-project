package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会计期间实体（对应 accounting_period 表）
 *
 * @author hakiraKafka
 */
@Data
public class AccountingPeriod {
    /** 会计期间 YYYYMM */
    private String period;
    /** 状态：OPEN=未结账 CLOSING=结账中 CLOSED=已结账 */
    private String status;
    /** 结账人 */
    private String closedBy;
    /** 结账时间 */
    private LocalDateTime closedAt;
    /** 乐观锁版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
