package com.hakira.ledger.api.report;

import com.hakira.ledger.api.dto.report.BalanceSheetResponse;
import com.hakira.ledger.api.dto.report.CashFlowResponse;
import com.hakira.ledger.api.dto.report.IncomeStatementResponse;
import com.hakira.ledger.api.dto.report.LedgerAccountItem;
import com.hakira.ledger.api.dto.report.LedgerEntryItem;

import java.util.List;

/**
 * 财务报表服务接口 — 三大报表 + 账簿（总账/明细账/日记账）
 */
public interface IReportService {
    /** 资产负债表 */
    BalanceSheetResponse getBalanceSheet(String period);

    /** 利润表 */
    IncomeStatementResponse getIncomeStatement(String period);

    /** 现金流量表 */
    CashFlowResponse getCashFlow(String period);

    /** 总账（科目汇总 + 明细） */
    List<LedgerAccountItem> getLedger(String period);

    /** 明细账（某科目逐笔分录） */
    List<LedgerEntryItem> getDetailLedger(String subjectCode, String period);

    /** 日记账（序时全部分录） */
    List<LedgerEntryItem> getJournal(String period);
}
