package com.hakira.market.order.rockermq.consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.order.rockermq.consumer
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  22:26:31
 * @Description: 标签过滤消费者
 * @Version: 1.0
 */
public class OrderMqTagFilterConsumer {
    public static void main(String[] args) {
        // 创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hakira-market-order-tagFilter-consumer");
        // 指定NameServer地址
        consumer.setNamesrvAddr("127.0.0.1:9876");
        try {
            String tagsFilterSql = "TAGS is not null " +
                    "and TAGS in ('tagA','tagC') " +
                    "and (a is not null and a between 0 and 3)";
            MessageSelector messageSelector = MessageSelector.bySql(tagsFilterSql);
            consumer.subscribe("hakira-market-order-tagFilter-topic", messageSelector);
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.registerMessageListener(getMessageListener());
            consumer.start();
            System.out.println("consumer started.%n");
        } catch (MQClientException e) {
            System.out.println(ExceptionUtils.getStackTrace(e));
//        } finally {
//            consumer.shutdown();
        }
    }

    private static MessageListenerConcurrently getMessageListener() {
        // 创建一个异步消息监听器
        MessageListenerConcurrently messageListenerConcurrently = new MessageListenerConcurrently() {
            @Override
            /**
             * 异步消费消息
             * @param msgs 消息列表
             * @param context 消费上下文
             * @return 消费结果
             */
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    // 遍历消息列表
                    for (MessageExt msg : msgs) {
                        // 获取消息主题
                        String topic = msg.getTopic();
                        // 获取消息标签
                        String tags = msg.getTags();
                        // 获取消息体内容，并转换编码为utf-8
                        String msgBody = new String(msg.getBody(), "utf-8");
                        String msgProperty = msg.getProperty("a");
                        // 创建一个字符串缓冲区，用于存储收到的消息信息
                        StringBuffer stringBuffer = new StringBuffer("收到消息：");
                        // 将消息信息追加到字符串缓冲区
                        stringBuffer.append("topic: ").append(topic)
                                .append("===tags: ").append(tags)
                                .append("===a: ").append(msgProperty)
                                .append("===msgBody: ").append(msgBody).append("]");
                        // 打印字符串缓冲区中的消息信息
                        System.out.println(stringBuffer.toString());
                    }
                    // 返回消费成功结果
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (UnsupportedEncodingException e) {
                    // 打印捕获的异常的堆栈跟踪信息
                    System.out.println(ExceptionUtils.getStackTrace(e));
                    // 返回稍后重新消费结果
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        };
        // 返回异步消息监听器对象
        return messageListenerConcurrently;
    }
}
