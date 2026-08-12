package com.hakira.ledger.entry.rockermq.producer;

import com.hakira.ledger.entry.rockermq.producer.splitter.MessageListSplitter;
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
 * @Description: 批量消息生产者 + 消息列表拆分器 ===== 处理大批量消息性能瓶颈问题
 * @Version: 1.0
 */
public class OrderMqBatchProducer {
    public static void main(String[] args) {
        // 创建一个消息生产者，并设置一个消息生产者组
        DefaultMQProducer producer = new DefaultMQProducer("hakira-ledger-entry-batch-producer");
        // 指定 NameServer 地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        try {
            // 启动消息生产者
            producer.start();
            // 这个函数是Java中的一个ArrayList类的构造函数。它接受一个参数initialCapacity，表示初始容量。
            // 这个函数会创建一个ArrayList对象，并分配足够的内部数组空间以容纳指定数量的元素。
            // 在添加元素时，ArrayList会根据需要动态调整内部数组的大小。
            List<Message> messageList = new ArrayList<>(1000 * 1000);// 初始化消息列表100万条消息
            for (int i = 0; i < 1000 * 1000; i++) {
                Message message = new Message(
                        "hakira-ledger-entry-batch-topic",
                        "hakira-ledger-entry-batch-tag",
                        "hakira-ledger-entry-batch-key",
                        ("hakira-ledger-entry-batch-body:" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                messageList.add(message);
            }
            // 消息列表拆分器
            MessageListSplitter splitter = new MessageListSplitter(messageList);// 把总消息列表拆分成若干个子列表，每个子列表的大小不超过1M
            while (splitter.hasNext()) {// 判断是否还有下一个元素
                List<Message> listItem = splitter.next();// 获取下一个元素
                SendResult sendResult = producer.send(listItem);// 批量消息发送，满足collection集合类型的都可以发送
                System.out.println(sendResult);
                Thread.sleep(1000); // 每隔1秒发送一次
            }
        } catch (MQClientException | UnsupportedEncodingException | RemotingException | MQBrokerException |
                 InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            producer.shutdown();
        }
    }
}
