package com.hakira.ledger.api.dto.budget;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算执行监控结果
 */
@Data
public class BudgetResponse {
    private String period;
    private List<BudgetItem> items;
    /** 预算合计 */
    private BigDecimal totalBudget;
    /** 实际合计 */
    private BigDecimal totalActual;
    /** 差异合计 */
    private BigDecimal totalVariance;
}
