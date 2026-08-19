package com.hakira.ledger.entry.pojo;

import lombok.Data;

/**
 * 分录行辅助核算关联实体（对应 journal_entry_line_aux 表）
 *
 * @author hakiraKafka
 */
@Data
public class JournalEntryLineAux {
    /** 分录行ID */
    private Long lineId;
    /** 维度编码 */
    private String dimensionCode;
    /** 维度值编码 */
    private String valueCode;
}
