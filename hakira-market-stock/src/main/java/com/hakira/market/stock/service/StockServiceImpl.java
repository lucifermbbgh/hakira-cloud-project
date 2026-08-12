package com.hakira.market.stock.service;

import com.hakira.market.api.stock.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.stock.service
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-25  22:38:07
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
public class StockServiceImpl implements IStockService {
    @Value("${server.port}")
    private String serverPort;

    /**
     * @description:创建订单
     * @author: hakiraKafka
     * @date: 2023/11/25 22:39
     * @param: id
     * @return: java.lang.String
     */
    @Override
    public String reduceStock(String orderId) {
        log.info(serverPort);
        StringBuffer stringBuffer = new StringBuffer("接收到订单：").append(orderId).append("，开始检出库存扣减数量……");
        return stringBuffer.append("扣减库存完成！端口号：").append(serverPort).toString();
    }

    /**
     * @description:增加库存
     * @author: hakiraKafka
     * @date: 2023/11/29 17:21
     * @param: orderId
     * @return: java.lang.String
     */
    @Override
    public String addStock(String orderId) {
        return null;
    }
}
