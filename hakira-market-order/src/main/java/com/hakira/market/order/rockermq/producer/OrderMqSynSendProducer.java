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
 * @Description: 同步消息生产者
 * @Version: 1.0
 */
public class OrderMqSynSendProducer {
    public static void main(String[] args) {
        // 创建生产者
        DefaultMQProducer producer = new DefaultMQProducer("hakira-market-order-producer");
        // 设置NameServer地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        try {
            // 启动生产者
            producer.start();
            for (int i = 0; i < 10; i++) {// 发送10条消息
                // 创建消息，并指定Topic，Tag和消息体
                Message message = new Message("hakira-market-order-topic",
                        "hakira-market-order-tag",
                        "hakira-market-order-key",
                        ("hakira-market-order-body" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                // 发送消息并收到发送结果
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
