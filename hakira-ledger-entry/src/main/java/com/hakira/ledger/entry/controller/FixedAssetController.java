package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.asset.IFixedAssetService;
import com.hakira.ledger.api.dto.asset.DepreciationResponse;
import com.hakira.ledger.api.dto.asset.DisposeResponse;
import com.hakira.ledger.api.dto.asset.FixedAssetRequest;
import com.hakira.ledger.api.dto.asset.FixedAssetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 固定资产控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping("/asset")
@Slf4j
@RequiredArgsConstructor
public class FixedAssetController {

    private final IFixedAssetService fixedAssetService;

    @PostMapping("/create")
    public Result<FixedAssetResponse> create(@RequestBody FixedAssetRequest request) {
        return Result.returnSuccess(fixedAssetService.create(request));
    }

    @GetMapping("/{assetCode}")
    public Result<FixedAssetResponse> get(@PathVariable("assetCode") String assetCode) {
        return Result.returnSuccess(fixedAssetService.get(assetCode));
    }

    @GetMapping("/list")
    public Result<List<FixedAssetResponse>> list() {
        return Result.returnSuccess(fixedAssetService.list());
    }

    @PostMapping("/depreciate")
    public Result<DepreciationResponse> depreciate(@RequestParam("period") String period) {
        return Result.returnSuccess(fixedAssetService.depreciate(period));
    }

    @PostMapping("/dispose/{assetCode}")
    public Result<DisposeResponse> dispose(@PathVariable("assetCode") String assetCode) {
        return Result.returnSuccess(fixedAssetService.dispose(assetCode));
    }
}
