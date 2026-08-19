package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.budget.IBudgetService;
import com.hakira.ledger.api.dto.budget.BudgetResponse;
import com.hakira.ledger.api.dto.budget.BudgetSetRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预算管理控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping("/budget")
@Slf4j
@RequiredArgsConstructor
public class BudgetController {

    private final IBudgetService budgetService;

    @PostMapping("/set")
    public Result<Void> set(@RequestBody BudgetSetRequest request) {
        budgetService.setBudget(request);
        return Result.returnSuccess(null);
    }

    @GetMapping("/query")
    public Result<BudgetResponse> query(@RequestParam("period") String period) {
        return Result.returnSuccess(budgetService.query(period));
    }

    @GetMapping("/variance")
    public Result<BudgetResponse> variance(@RequestParam("period") String period) {
        return Result.returnSuccess(budgetService.variance(period));
    }
}
