package com.hakira.market.stock.controller;

import com.hakira.common.pojo.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.stock.controller
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-29  21:34:40
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/test")
@RefreshScope
@Slf4j
public class TestController {
    @Value("${server.port}")
    private String port;

    @GetMapping("/timeout/{id}")
    public Result<String> testTimeout(@PathVariable("id") String id) {
        try {
            Thread.sleep(3000);
            System.out.println("延迟响应");
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        StringBuffer stringBuffer = new StringBuffer("延迟响应！端口号：[");
        stringBuffer.append(port).append("]=====id：[").append(id).append("]");
        return Result.returnSuccess(stringBuffer.toString());
    }
}
