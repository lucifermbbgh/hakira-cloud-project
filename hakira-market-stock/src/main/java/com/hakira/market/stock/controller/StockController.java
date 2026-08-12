package com.hakira.market.stock.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.market.api.stock.IStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.stock.controller
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-25  22:37:33
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/stock")
public class StockController {
    @Autowired
    private IStockService iStockService;

    @GetMapping("/reduce/{orderId}")
    public Result<String> reduceStock(@PathVariable("orderId") String orderId) {
        String result = iStockService.reduceStock(orderId);
        return Result.returnSuccess(result);
    }
}
