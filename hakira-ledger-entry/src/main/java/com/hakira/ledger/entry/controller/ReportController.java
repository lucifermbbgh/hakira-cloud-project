package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.dto.report.BalanceSheetResponse;
import com.hakira.ledger.api.dto.report.CashFlowResponse;
import com.hakira.ledger.api.dto.report.IncomeStatementResponse;
import com.hakira.ledger.api.report.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
