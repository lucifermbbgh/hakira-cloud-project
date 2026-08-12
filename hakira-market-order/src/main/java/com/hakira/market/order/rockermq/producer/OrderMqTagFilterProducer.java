package com.hakira.market.order.rockermq.producer;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.order.rockermq
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  21:35:00
 * @Description: 标签过滤消息生产者
 * @Version: 1.0
 */
public class OrderMqTagFilterProducer {
    public static void main(String[] args) {
        // 创建生产者
        DefaultMQProducer producer = new DefaultMQProducer("hakira-market-order-tagFilter-producer");
        // 设置NameServer地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        try {
            // 启动生产者
            producer.start();
            // 发送的消息的标签
            String[] tags = new String[]{"tagA", "tagB", "tagC"};
            // 发送10条消息
            for (int i = 0; i < 10; i++) {
                // 创建消息
                Message message = new Message("hakira-market-order-tagFilter-topic",
                        tags[i % tags.length],
                        "hakira-market-order-tagFilter-key",
                        ("hakira-market-order-tagFilter-body" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                // 设置消息属性
//                message.putUserProperty("a", String.valueOf(i % (tags.length + 1)));
                message.putUserProperty("a", String.valueOf(i));
                // 发送消息
                SendResult sendResult = producer.send(message);
                System.out.println(sendResult);
            }
        } catch (MQClientException | UnsupportedEncodingException | RemotingException | MQBrokerException |
                 InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭生产者
            producer.shutdown();
        }
    }
}
