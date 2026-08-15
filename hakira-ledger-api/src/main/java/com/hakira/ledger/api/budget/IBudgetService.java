package com.hakira.ledger.api.budget;

import com.hakira.ledger.api.dto.budget.BudgetResponse;
import com.hakira.ledger.api.dto.budget.BudgetSetRequest;

/**
 * 预算管理服务接口 — 预算编制 / 执行监控 / 差异分析
 */
public interface IBudgetService {
    /** 编制/更新预算 */
    void setBudget(BudgetSetRequest request);

    /** 执行监控（预算 vs 实际 vs 差异） */
    BudgetResponse query(String period);

    /** 差异分析（超支科目明细） */
    BudgetResponse variance(String period);
}
