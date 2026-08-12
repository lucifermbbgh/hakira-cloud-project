package com.hakira.ledger.entry.rockermq.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.entry.rockermq
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  21:35:00
 * @Description: 同步消息生产者
 * @Version: 1.0
 */
@Slf4j
public class OrderBaseMqProducer {

    public static DefaultMQProducer initProducer() {
        // 创建生产者，指定生产者组名为hakira-ledger-entry-producer
        DefaultMQProducer producer = new DefaultMQProducer("hakira-ledger-entry-producer");
        // 设置NameServer地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 设置主题在没一个Broker默认的队列数（对于新创建的Topic有效，默认队列数为8）
        producer.setDefaultTopicQueueNums(8);
        // 设置发送超时时间，单位ms毫秒，默认30000ms，设置为1000ms
        producer.setSendMsgTimeout(1000);
        // 设置消息压缩方式，默认不压缩，单位KB，设置压缩阈值为10M
        producer.setCompressMsgBodyOverHowmuch(1024 * 10);
        // 设置重试次数，默认为2次，设置为3次，表示发送失败后，最多重试3次
        producer.setRetryTimesWhenSendFailed(3);
        // 设置异步发送失败重试次数，默认为2次，设置为3次，表示发送失败后，最多重试3次
        producer.setRetryTimesWhenSendAsyncFailed(3);
        // 设置是否重试发送到另一个Broker，默认为false，设置为true，表示发送失败后，会尝试发送到另一个Broker
        producer.setRetryAnotherBrokerWhenNotStoreOK(true);
        // 设置消息最大长度，默认为4M，设置为10M
        producer.setMaxMessageSize(1024 * 1024 * 10);
        try {
            // 启动生产者
            producer.start();
        } catch (MQClientException e) {
            log.error("消息生产者创建失败，异常信息：{}", ExceptionUtils.getStackTrace(e));
        }
        return producer;
    }

    public static void main(String[] args) {
        DefaultMQProducer producer = initProducer();
        try {
            // 获取指定Topic下的所有消息队列列表
            List<MessageQueue> messageQueueList = producer.fetchPublishMessageQueues("hakira-ledger-entry-topic");

            for (int i = 0; i < messageQueueList.size(); i++) {
                // 获取消息队列ID
                System.out.println(messageQueueList.get(i).getQueueId());
            }

            for (int i = 0; i < 10; i++) {// 发送10条消息

                final int index = i;

                Message message = new Message("hakira-ledger-entry-topic",
                        "hakira-ledger-entry-tag",
                        "hakira-ledger-entry-key",
                        ("hakira!").getBytes(RemotingHelper.DEFAULT_CHARSET));

                // 单向发送消息
                producer.sendOneway(message);
                // 单向发送消息，指定消息队列，指定消息队列选择器MessageQueueSelector
                producer.sendOneway(message, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        return mqs.get(0);
                    }
                }, null);
                // 单向发送消息，直接指定消息队列
                producer.sendOneway(message, messageQueueList.get(0));

                // 同步发送消息
                SendResult sendResult = producer.send(message);
                // 同步发送消息，指定发送超时时间，单位ms毫秒，默认3000ms
                SendResult sendResult1 = producer.send(message, 1000 * 10);
                // 同步发送消息，指定消息队列，指定消息队列选择器MessageQueueSelector
                SendResult sendResult2 = producer.send(message, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        return mqs.get(0);
                    }
                }, null);
                // 同步发送消息，直接指定消息队列
                SendResult sendResult3 = producer.send(message, messageQueueList.get(0));

                // 异步发送消息
                producer.send(message, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.printf("%-10d OK %s %n", index, sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        System.out.printf("%-10d ERROR %s %n", index, ExceptionUtils.getStackTrace(e));
                    }
                });
                // 异步发送消息，指定发送超时时间，单位ms毫秒，默认3000ms，设置为1000ms
                producer.send(message, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.printf("%-10d OK %s %n", index, sendResult.getMsgId());
                    }

                    @Override

                    public void onException(Throwable e) {
                        System.out.printf("%-10d ERROR %s %n", index, ExceptionUtils.getStackTrace(e));
                    }
                }, 1000 * 10);

                // 异步发送消息，指定消息队列，指定消息队列选择器MessageQueueSelector
                producer.send(message, new MessageQueueSelector() {
                    @Override
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                        return mqs.get(0);
                    }
                }, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.printf("%-10d OK %s %n", index, sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        System.out.printf("%-10d ERROR %s %n", index, ExceptionUtils.getStackTrace(e));
                    }
                }, 1000 * 10);

                // 异步发送消息，直接指定消息队列
                producer.send(message, messageQueueList.get(0), new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        System.out.printf("%-10d OK %s %n", index, sendResult.getMsgId());
                    }

                    @Override
                    public void onException(Throwable e) {
                        System.out.printf("%-10d ERROR %s %n", index, ExceptionUtils.getStackTrace(e));
                    }
                }, 1000 * 10);
            }
        } catch (MQClientException | UnsupportedEncodingException | RemotingException | MQBrokerException |
                 InterruptedException e) {
            log.error("发送消息失败，错误信息：{}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        } finally {
            // 关闭生产者
            producer.shutdown();
        }
    }
}
