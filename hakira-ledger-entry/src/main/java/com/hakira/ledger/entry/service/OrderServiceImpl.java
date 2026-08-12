package com.hakira.ledger.entry.service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.hakira.ledger.api.order.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.service
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-17  22:13:40
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {
    @Value("${server.port}")
    private String serverPort;

    @Override
    public String createOrder(String orderId) {
        log.info(serverPort);
        StringBuffer stringBuffer = new StringBuffer("welcome hakira market!");
        return stringBuffer.append("订单号为：").append(orderId).append(" 端口号为：").append(serverPort).toString();
    }

    /**
     * @param orderId
     * @description:查询订单详情
     * @author: hakiraKafka
     * @date: 2023/11/28 12:08
     * @param: orderId
     * @return: java.lang.String
     */
    @Override
    @SentinelResource("IOrderService.getOrderInfo")
    public String getOrderInfo(String orderId) {
        return new StringBuffer("链路请求进入，orderId为：").append(orderId).toString();
    }
}
