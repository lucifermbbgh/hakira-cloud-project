package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.aging.IBadDebtService;
import com.hakira.ledger.api.dto.aging.BadDebtProvisionResponse;
import com.hakira.ledger.api.dto.aging.BadDebtRequest;
import com.hakira.ledger.api.dto.aging.BadDebtResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 坏账处理控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping("/baddebt")
@Slf4j
@RequiredArgsConstructor
public class BadDebtController {

    private final IBadDebtService badDebtService;

    @PostMapping("/provision")
    public Result<BadDebtProvisionResponse> provision(@RequestParam("asOf") String asOfDate) {
        return Result.returnSuccess(badDebtService.provision(asOfDate));
    }

    @PostMapping("/writeoff")
    public Result<BadDebtResponse> writeoff(@RequestBody BadDebtRequest request) {
        return Result.returnSuccess(badDebtService.writeoff(request));
    }

    @PostMapping("/recover")
    public Result<BadDebtResponse> recover(@RequestBody BadDebtRequest request) {
        return Result.returnSuccess(badDebtService.recover(request));
    }
}
