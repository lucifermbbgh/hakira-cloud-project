package com.hakira.market.order.rockermq.producer;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.order.rockermq
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  21:35:00
 * @Description: 单向消息生产者
 * @Version: 1.0
 */
public class OrderMqOnewaySendProducer {
    public static void main(String[] args) {
        // 创建生产者对象
        DefaultMQProducer producer = new DefaultMQProducer("hakira-market-order-producer");
        // 设置NameServer的地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        try {
            // 启动生产者
            producer.start();
            for (int i = 0; i < 10; i++) {// 发送10条消息
                // 创建消息对象
                Message message = new Message("hakira-market-order-topic",
                        "hakira-market-order-tag",
                        "hakira-market-order-key",
                        ("hakira-market-order-body" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                // 发送单向消息（无结果）
                producer.sendOneway(message);
            }
        } catch (MQClientException | UnsupportedEncodingException | RemotingException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭生产者
            producer.shutdown();
        }
    }
}
