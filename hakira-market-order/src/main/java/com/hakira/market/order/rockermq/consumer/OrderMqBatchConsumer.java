package com.hakira.market.order.rockermq.consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.order.rockermq.consumer
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  22:26:31
 * @Description: 批量消费者
 * @Version: 1.0
 */
public class OrderMqBatchConsumer {
    public static void main(String[] args) {
        // 创建一个消息消费者，并设置一个消息消费者组
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hakira-market-order-batch-consumer");
        // 设置NameServer的地址
        consumer.setNamesrvAddr("127.0.0.1:9876");
        try {
            // 设置消息消费者订阅的主题和标签
            consumer.subscribe("hakira-market-order-batch-topic", "*");
            // 设置消息消费模式为集群消费
            consumer.setMessageModel(MessageModel.CLUSTERING);
            // 注册消息监听器
            consumer.registerMessageListener(getMessageListener());
            // 启动消息消费者
            consumer.start();
            System.out.println("consumer started.%n");
        } catch (MQClientException e) {
            System.out.println(ExceptionUtils.getStackTrace(e));
//        } finally {
//            consumer.shutdown();
        }
    }

    /**
     * @description: 消息监听器
     * @author: hakiraKafka
     * @date: 2023/12/12 17:58
     * @return: org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently
     **/
    private static MessageListenerConcurrently getMessageListener() {
        MessageListenerConcurrently messageListenerConcurrently = new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                    // 消费成功
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    System.out.println(ExceptionUtils.getStackTrace(e));
                    // 消费失败，挂起当前队列，稍后再试
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        };
        return messageListenerConcurrently;
    }
}
