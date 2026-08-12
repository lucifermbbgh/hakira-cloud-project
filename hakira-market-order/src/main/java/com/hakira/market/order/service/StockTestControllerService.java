package com.hakira.market.order.service;

import com.hakira.common.pojo.common.Result;
import com.hakira.market.order.service.impl.StockTestControllerServiceImpl;
import org.springframework.cloud.openfeign.FeignClient;
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
@FeignClient(value = "hakira-market-stock-service", fallback = StockTestControllerServiceImpl.class)// 使用Feign客户端调用远程服务的注解
public interface StockTestControllerService {
    @GetMapping("/stock/reduce/{orderId}")
    public Result<String> reduceStock(@PathVariable("orderId") String orderId);

    @GetMapping("/test/timeout/{id}")
    public Result<String> testTimeout(@PathVariable("id") String id);
}
