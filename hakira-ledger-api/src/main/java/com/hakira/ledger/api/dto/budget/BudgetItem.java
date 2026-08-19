package com.hakira.ledger.api.dto.budget;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算项（预算 vs 实际 vs 差异）
 */
@Data
public class BudgetItem {
    private String subjectCode;
    private String subjectName;
    /** 预算金额 */
    private BigDecimal budgetAmount;
    /** 实际发生额 */
    private BigDecimal actualAmount;
    /** 差异 = 实际 − 预算 */
    private BigDecimal variance;
    /** 是否超支（费用类差异>0 / 收入类差异<0） */
    private boolean overBudget;
}
