package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.cost.ICostService;
import com.hakira.ledger.api.dto.cost.CostAllocateResponse;
import com.hakira.ledger.api.dto.cost.CostSheetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 成本核算控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping("/cost")
@Slf4j
@RequiredArgsConstructor
public class CostController {

    private final ICostService costService;

    @PostMapping("/allocate-overhead")
    public Result<CostAllocateResponse> allocateOverhead(@RequestParam("period") String period) {
        return Result.returnSuccess(costService.allocateOverhead(period));
    }

    @GetMapping("/cost-sheet")
    public Result<CostSheetResponse> getCostSheet(@RequestParam("period") String period) {
        return Result.returnSuccess(costService.getCostSheet(period));
    }
}
