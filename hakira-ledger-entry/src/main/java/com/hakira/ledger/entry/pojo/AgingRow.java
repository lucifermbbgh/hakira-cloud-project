package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账龄分析聚合行（往来单位 + 发生日期 + 借贷发生额）
 *
 * @author hakiraKafka
 */
@Data
public class AgingRow {
    /** 往来单位编码 */
    private String valueCode;
    /** 往来单位名称 */
    private String valueName;
    /** 发生日期 */
    private LocalDate entryDate;
    /** 借方发生额 */
    private BigDecimal debitAmount;
    /** 贷方发生额 */
    private BigDecimal creditAmount;
}
