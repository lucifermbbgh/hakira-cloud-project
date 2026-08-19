package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.aging.IAgingService;
import com.hakira.ledger.api.dto.aging.AgingResponse;
import com.hakira.ledger.api.dto.aging.PartnerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 往来账龄控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class AgingController {

    private final IAgingService agingService;

    @GetMapping("/partner/list")
    public Result<List<PartnerResponse>> listPartners(@RequestParam("dimension") String dimension) {
        return Result.returnSuccess(agingService.listPartners(dimension));
    }

    @GetMapping("/aging/receivable")
    public Result<AgingResponse> getReceivableAging(@RequestParam("asOf") String asOfDate) {
        return Result.returnSuccess(agingService.getReceivableAging(asOfDate));
    }

    @GetMapping("/aging/payable")
    public Result<AgingResponse> getPayableAging(@RequestParam("asOf") String asOfDate) {
        return Result.returnSuccess(agingService.getPayableAging(asOfDate));
    }
}
