package com.hakira.market.api.order;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.service.order
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-17  22:10:11
 * @Description: TODO
 * @Version: 1.0
 */
public interface IOrderService {
    /**
     * @description:创建订单
     * @author: hakiraKafka
     * @date: 2023/11/25 22:51
     * @param: orderId
     * @return: java.lang.String
     **/
    String createOrder(String orderId);

    /**
     * @description:查询订单详情
     * @author: hakiraKafka
     * @date: 2023/11/28 12:08
     * @param: orderId
     * @return: java.lang.String
     **/
    String getOrderInfo(String orderId);
}
