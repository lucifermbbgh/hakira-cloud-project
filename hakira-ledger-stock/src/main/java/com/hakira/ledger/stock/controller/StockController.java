package com.hakira.ledger.stock.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.dto.stock.StockMovementRequest;
import com.hakira.ledger.api.dto.stock.StockMovementResponse;
import com.hakira.ledger.api.dto.stock.StockSnapshotResponse;
import com.hakira.ledger.api.stock.IStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@Slf4j
@RequiredArgsConstructor
public class StockController {
    private final IStockService stockService;

    @PostMapping("/inbound")
    public Result<StockMovementResponse> inbound(@RequestBody StockMovementRequest request) {
        return Result.returnSuccess(stockService.recordInbound(request));
    }

    @PostMapping("/outbound")
    public Result<StockMovementResponse> outbound(@RequestBody StockMovementRequest request) {
        return Result.returnSuccess(stockService.recordOutbound(request));
    }

    @GetMapping("/snapshot/{itemCode}")
    public Result<StockSnapshotResponse> snapshot(@PathVariable("itemCode") String itemCode) {
        return Result.returnSuccess(stockService.getSnapshot(itemCode));
    }

    @GetMapping("/movements/{itemCode}")
    public Result<List<StockMovementResponse>> movements(
            @PathVariable("itemCode") String itemCode,
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate) {
        return Result.returnSuccess(stockService.getMovements(itemCode, fromDate, toDate));
    }
}
