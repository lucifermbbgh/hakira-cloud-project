package com.hakira.market.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.hakira.common.pojo.common.Result;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.market.api.order.IOrderService;
import com.hakira.market.order.handler.FallbackExceptionHandler;
import com.hakira.market.order.service.StockTestControllerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.order.controller
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-25  22:57:19
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/order")
@RefreshScope// 支持Nacos的动态刷新功能
@Slf4j
public class OrderController {
    @Autowired
    private IOrderService iOrderService;
    //    @Autowired
//    private StockTestService stockTestService;
    @Autowired
    private StockTestControllerService stockTestControllerService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${service-url.nacos-user-service}")
    private String orderServerUrl;

    @GetMapping("/create")
    public String createOrder() {
        String orderId = IdGeneratorUtil.getId();
        String order = iOrderService.createOrder(orderId);
        String template = restTemplate.getForObject(orderServerUrl + "/stock/reduce", String.class, orderId);
        StringBuffer stringBuffer = new StringBuffer(order);
        return stringBuffer.append("\r\n").append(template).toString();
    }

    @GetMapping("/create2")
    public Result<String> createOrder2() {
        String orderId = IdGeneratorUtil.getId();
        String order = iOrderService.createOrder(orderId);
        return Result.returnSuccess(order);
    }

    @GetMapping("/reduceStock/{orderId}")
    @SentinelResource(value = "order.reduceStock", fallbackClass = FallbackExceptionHandler.class, fallback = "handler2")
    public Result<String> reduceStock(@PathVariable("orderId") String orderId) {
        if (StringUtils.equals(orderId, "123")) {
            throw new NullPointerException("订单不存在");
        }
        return stockTestControllerService.reduceStock(orderId);
    }

    @GetMapping("/timeout/{id}")
    public Result<String> timeout(@PathVariable("id") String id) {
        return stockTestControllerService.testTimeout(id);
    }

}
