package com.hakira.ledger.entry.rockermq.producer;

import com.hakira.common.constants.OrderStatusEnum;
import com.hakira.ledger.entry.pojo.dto.MarketOrderInfoDto;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.entry.rockermq
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  21:35:00
 * @Description: 延时消息生产者
 * @Version: 1.0
 */
public class OrderMqSynSendDelayProducer {
    public static void main(String[] args) {
        DefaultMQProducer producer = new DefaultMQProducer("hakira-ledger-entry-producer");
        producer.setNamesrvAddr("127.0.0.1:9876");
        try {
            producer.start();
            List<MarketOrderInfoDto> orderList = getOrderList();
            for (int i = 0; i < orderList.size(); i++) {
//                Message message = new Message("hakira-ledger-entry-topic", "hakira-ledger-entry-tag", "hakira-ledger-entry-key", "hakira-ledger-entry-body".getBytes());
                String orderInfo = orderList.get(i).toString();
                Message message = new Message("hakira-ledger-entry-topic",
                        "hakira-ledger-entry-tag",
                        "hakira-ledger-entry-key",
                        orderInfo.getBytes(RemotingHelper.DEFAULT_CHARSET));
                // 设置延迟消息，分为18个等级，从1开始，1-18，对应的延迟时间为：1s,5s,10s,30s,1m,2m,3m,4m,5m,6m,7m,8m,9m,10m,20m,30m,1h,2h
                message.setDelayTimeLevel(3);
                SendResult sendResult = producer.send(message);
                System.out.println(sendResult);
            }
        } catch (MQClientException | UnsupportedEncodingException | RemotingException | MQBrokerException |
                 InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            producer.shutdown();
        }
    }

    private static List<MarketOrderInfoDto> getOrderList() {
        List<MarketOrderInfoDto> orderList = new ArrayList<>();
        // 创建一个int数组
        int[] arr = {1, 2, 3, 4};
        for (int i = 0; i < 16; i++) {
            MarketOrderInfoDto orderInfoDto = new MarketOrderInfoDto();
            orderInfoDto.setId(String.valueOf(arr[i % arr.length]));
            orderInfoDto.setStatus(arr[i % arr.length]);
            orderInfoDto.setOrderDesc(OrderStatusEnum.getDescriptionByCode(arr[i % arr.length]));
            orderInfoDto.setCreateDate(String.valueOf(System.currentTimeMillis()));
            orderList.add(orderInfoDto);
        }

        return orderList;
    }
}
