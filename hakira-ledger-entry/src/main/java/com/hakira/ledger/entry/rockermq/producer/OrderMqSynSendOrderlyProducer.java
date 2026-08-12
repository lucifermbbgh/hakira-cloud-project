package com.hakira.ledger.entry.rockermq.producer;

import com.hakira.common.constants.OrderStatusEnum;
import com.hakira.ledger.entry.pojo.dto.MarketOrderInfoDto;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
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
 * @Description: 顺序消息生产者
 * @Version: 1.0
 */
public class OrderMqSynSendOrderlyProducer {
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
                SendResult sendResult = producer.send(message, getMessageaQueueSelector(), orderList.get(i).getId());
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

    private static MessageQueueSelector getMessageaQueueSelector() {
        MessageQueueSelector messageQueueSelector = new MessageQueueSelector() {
            @Override
            public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                String id = arg.toString();
                Long aLong = Long.valueOf(id);
                long index = aLong % mqs.size();
                return mqs.get((int) index);
            }
        };
        return messageQueueSelector;
    }
}
