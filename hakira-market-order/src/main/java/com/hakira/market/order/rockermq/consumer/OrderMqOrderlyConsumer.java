package com.hakira.market.order.rockermq.consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.order.rockermq.consumer
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  22:26:31
 * @Description: 顺序消费者
 * @Version: 1.0
 */
public class OrderMqOrderlyConsumer {
    public static void main(String[] args) {
        // 创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hakira-market-order-consumer");
        // 设置nameserver地址
        consumer.setNamesrvAddr("127.0.0.1:9876");
        // 设置从哪里开始消费
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        try {
            // 设置消费的topic和tag
            consumer.subscribe("hakira-market-order-topic", "*");
            // 设置消息模式
            consumer.setMessageModel(MessageModel.CLUSTERING);
            // 注册监听器
            consumer.registerMessageListener(getMessageListenerOrderly());
            // 启动消费者
            consumer.start();
            System.out.println("consumer started.%n");
        } catch (MQClientException e) {
            System.out.println(ExceptionUtils.getStackTrace(e));
//        } finally {
//            consumer.shutdown();
        }
    }

    /**
     * @description: 顺序消息监听器
     * @author: hakiraKafka
     * @date: 2023/12/12 18:13
     * @return: org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly
     **/
    private static MessageListenerOrderly getMessageListenerOrderly() {
        MessageListenerOrderly messageListenerOrderly = new MessageListenerOrderly() {
            Random random = new Random();

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                try {
                    context.setAutoCommit(true);// 设置自动提交
                    for (MessageExt msg : msgs) {
                        String topic = msg.getTopic();
                        String tags = msg.getTags();
                        String msgBody = new String(msg.getBody(), "utf-8");
                        StringBuffer stringBuffer = new StringBuffer("收到消息：");
                        stringBuffer.append("consumeThread: ").append(Thread.currentThread().getName())
                                .append("===queueId: ").append(msg.getQueueId())
                                .append("content: [")
                                .append("topic: ").append(topic)
                                .append("===tags: ").append(tags)
                                .append("===msgBody: ").append(msgBody).append("]");
                        System.out.println(stringBuffer.toString());
                    }
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(3000));
                    return ConsumeOrderlyStatus.SUCCESS;
                } catch (Exception e) {
                    System.out.println(ExceptionUtils.getStackTrace(e));
                    // 如果出现异常，挂起当前队列，稍后再处理这批消息，而不是放到重试队列
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
        };

        return messageListenerOrderly;
    }
}
