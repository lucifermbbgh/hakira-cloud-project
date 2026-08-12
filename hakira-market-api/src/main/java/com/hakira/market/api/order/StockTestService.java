package com.hakira.market.api.order;

import com.hakira.common.pojo.common.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.api.order
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-29  21:38:18
 * @Description: TODO
 * @Version: 1.0
 */
@Service
//@FeignClient(value = "hakira-market-stock-service", fallback = StockTestServiceImpl.class)
public interface StockTestService {
    @GetMapping("/stock/reduce/{orderId}")
    public Result<String> reduceStock(@PathVariable("orderId") String orderId);

    @GetMapping("/test/timeout/{id}")
    public Result<String> testTimeout(@PathVariable("id") String id);
}
