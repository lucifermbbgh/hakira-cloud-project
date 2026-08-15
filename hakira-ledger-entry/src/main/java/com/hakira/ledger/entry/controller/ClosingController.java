package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.closing.IClosingService;
import com.hakira.ledger.api.dto.closing.AccountBalanceResponse;
import com.hakira.ledger.api.dto.closing.ClosingResponse;
import com.hakira.ledger.api.dto.closing.TrialBalanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 期末结账控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping("/closing")
@Slf4j
@RequiredArgsConstructor
public class ClosingController {

    private final IClosingService closingService;

    @GetMapping("/balance")
    public Result<List<AccountBalanceResponse>> getBalance(@RequestParam("period") String period) {
        return Result.returnSuccess(closingService.getBalance(period));
    }

    @GetMapping("/trial-balance")
    public Result<TrialBalanceResponse> getTrialBalance(@RequestParam("period") String period) {
        return Result.returnSuccess(closingService.getTrialBalance(period));
    }

    @PostMapping("/profit-transfer")
    public Result<ClosingResponse> profitTransfer(@RequestParam("period") String period) {
        return Result.returnSuccess(closingService.profitTransfer(period));
    }

    @PostMapping("/close")
    public Result<ClosingResponse> close(@RequestParam("period") String period) {
        return Result.returnSuccess(closingService.close(period));
    }
}
