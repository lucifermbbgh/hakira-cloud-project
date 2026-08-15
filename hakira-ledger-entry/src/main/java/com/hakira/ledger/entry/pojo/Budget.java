package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算实体（对应 budget 表）
 *
 * @author hakiraKafka
 */
@Data
public class Budget {
    /** 会计期间 YYYYMM */
    private String period;
    /** 科目编码 */
    private String subjectCode;
    /** 预算金额 */
    private BigDecimal budgetAmount;
    /** 乐观锁版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
