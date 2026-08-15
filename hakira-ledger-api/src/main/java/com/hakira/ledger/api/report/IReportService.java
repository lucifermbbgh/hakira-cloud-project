package com.hakira.ledger.api.report;

import com.hakira.ledger.api.dto.report.BalanceSheetResponse;
import com.hakira.ledger.api.dto.report.CashFlowResponse;
import com.hakira.ledger.api.dto.report.IncomeStatementResponse;

/**
 * 财务报表服务接口 — 三大报表
 */
public interface IReportService {
    /** 资产负债表 */
    BalanceSheetResponse getBalanceSheet(String period);

    /** 利润表 */
    IncomeStatementResponse getIncomeStatement(String period);

    /** 现金流量表 */
    CashFlowResponse getCashFlow(String period);
}
