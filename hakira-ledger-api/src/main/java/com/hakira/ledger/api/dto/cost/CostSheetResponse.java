package com.hakira.ledger.api.dto.cost;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成本计算单（料工费 + 总成本）
 */
@Data
public class CostSheetResponse {
    private String period;
    /** 直接材料 DM */
    private BigDecimal directMaterial;
    /** 直接人工 DL */
    private BigDecimal directLabor;
    /** 制造费用 MO */
    private BigDecimal manufacturingOverhead;
    /** 总成本 = 直接材料 + 直接人工 + 制造费用 */
    private BigDecimal totalCost;
}
