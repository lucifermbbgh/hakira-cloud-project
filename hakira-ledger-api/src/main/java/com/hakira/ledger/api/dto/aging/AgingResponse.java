package com.hakira.ledger.api.dto.aging;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账龄分析结果
 */
@Data
public class AgingResponse {
    /** 截止日期 */
    private String asOfDate;
    /** 类型：RECEIVABLE 应收 / PAYABLE 应付 */
    private String type;
    /** 明细 */
    private List<AgingItem> items;
    /** 总余额 */
    private BigDecimal totalBalance;
    /** 超龄总余额（90 天以上） */
    private BigDecimal totalOverdue;
}
