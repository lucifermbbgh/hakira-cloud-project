package com.hakira.ledger.entry.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.hakira.common.exception.ServiceFailException;
import com.hakira.common.pojo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.config.controller
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-26  17:06:17
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/sentinel")
@RefreshScope// 支持Nacos的动态刷新功能
@Slf4j
public class ConfigController {
    @Value("${hakira.sentinel.host}")
    private String sentinelHost;

    @Value("${hakira.sentinel.port}")
    private String sentinelPort;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/timeout/{id}")
    @SentinelResource(value = "sentinel.timeout")
    public Result<String> testTimeout(@PathVariable("id") String id) {

        return Result.returnSuccess(id);
    }

    @GetMapping("/info1")
    @SentinelResource(value = "sentinel.info",
            exceptionsToIgnore = {NullPointerException.class})
    public Result<String> getOrderInfo1() {
        return Result.returnSuccess("查询成功！端口号：" + serverPort);
    }

    @GetMapping("/info2")
    @SentinelResource(value = "sentinel.info",
            exceptionsToIgnore = {NullPointerException.class})
    public Result<String> getOrderInfo2(String orderId) throws Exception {
        if (StringUtils.equals(orderId, "123")) {
            throw new NullPointerException("空空空空空空空空空空");
        }
        if (StringUtils.equals(orderId, "123456")) {
            throw new ServiceFailException(ServiceFailException.DEFAULT_ERROR_CODE, ServiceFailException.DEFAULT_ERROR_INFO);
        }
        return Result.returnSuccess("查询成功！端口号：" + serverPort);
    }

    @GetMapping("/testHandler1")
    @SentinelResource(value = "testHandler1")
    public Result<String> testHandler1() {
        return Result.returnSuccess("123456789");
    }

    @GetMapping("/testHandler2")
    @SentinelResource(value = "testHandler2")
    public Result<String> testHandler2(String id) {
        return Result.returnSuccess(id);
    }

    @GetMapping("/testHotKey")
    @SentinelResource(value = "sentinel.testHotKey", blockHandler = "handler_hotKey")
    public Result<String> testHotKey(@RequestParam(value = "hot1", required = false) String hot1,
                                     @RequestParam(value = "hot2", required = false) String hot2,
                                     @RequestParam(value = "hot3", required = false) String hot3) {
        StringBuffer result = new StringBuffer("进入testHotKey");
        if (StringUtils.isNotBlank(hot1)) {
            result.append(hot1).append("=====");
        }
        if (StringUtils.isNotBlank(hot2)) {
            result.append(hot2).append("=====");
        }
        if (StringUtils.isNotBlank(hot3)) {
            result.append(hot3).append("=====");
        }

        return Result.returnSuccess(result.toString());
    }

    public Result<String> handler_hotKey(String hot1, String hot2, String hot3, BlockException blockException) {
        return Result.returnFail("99999", "系统繁忙，请稍后再试！", "系统繁忙，请稍后再试！");
    }

    @GetMapping("/infoA")
    public String getConfigInfoA() {
//        try {
//            TimeUnit.MILLISECONDS.sleep(800);
//        } catch (InterruptedException e) {
//            log.error(ExceptionUtils.getStackTrace(e));
//        }
        return new StringBuffer(sentinelHost).append("===A===").append(sentinelPort).toString();
    }

    @GetMapping("/infoB")
    public String getConfigInfoB() {
//        try {
//            TimeUnit.MILLISECONDS.sleep(800);
//        } catch (InterruptedException e) {
//            log.error(ExceptionUtils.getStackTrace(e));
//        }
        return new StringBuffer(sentinelHost).append("===B===").append(sentinelPort).toString();
    }
}
