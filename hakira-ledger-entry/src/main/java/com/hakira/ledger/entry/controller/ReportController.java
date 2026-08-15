package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.dto.report.BalanceSheetResponse;
import com.hakira.ledger.api.dto.report.CashFlowResponse;
import com.hakira.ledger.api.dto.report.IncomeStatementResponse;
import com.hakira.ledger.api.dto.report.LedgerAccountItem;
import com.hakira.ledger.api.dto.report.LedgerEntryItem;
import com.hakira.ledger.api.report.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 财务报表控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping("/report")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/balance-sheet")
    public Result<BalanceSheetResponse> getBalanceSheet(@RequestParam("period") String period) {
        return Result.returnSuccess(reportService.getBalanceSheet(period));
    }

    @GetMapping("/income-statement")
    public Result<IncomeStatementResponse> getIncomeStatement(@RequestParam("period") String period) {
        return Result.returnSuccess(reportService.getIncomeStatement(period));
    }

    @GetMapping("/cash-flow")
    public Result<CashFlowResponse> getCashFlow(@RequestParam("period") String period) {
        return Result.returnSuccess(reportService.getCashFlow(period));
    }

    @GetMapping("/ledger")
    public Result<List<LedgerAccountItem>> getLedger(@RequestParam("period") String period) {
        return Result.returnSuccess(reportService.getLedger(period));
    }

    @GetMapping("/detail-ledger")
    public Result<List<LedgerEntryItem>> getDetailLedger(@RequestParam("subjectCode") String subjectCode,
                                                         @RequestParam("period") String period) {
        return Result.returnSuccess(reportService.getDetailLedger(subjectCode, period));
    }

    @GetMapping("/journal")
    public Result<List<LedgerEntryItem>> getJournal(@RequestParam("period") String period) {
        return Result.returnSuccess(reportService.getJournal(period));
    }
}
