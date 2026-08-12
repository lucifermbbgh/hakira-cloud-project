package com.hakira.market.order.rockermq.consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
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
 * @Description: 均衡消费者
 * @Version: 1.0
 */
public class OrderMqBalanceConsumer {
    public static void main(String[] args) {
        // 创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hakira-market-order-consumer");
        // 指定NameServer地址
        consumer.setNamesrvAddr("127.0.0.1:9876");
        try {
            // 订阅消息
            consumer.subscribe("hakira-market-order-topic", "*");
            // 集群消费
            consumer.setMessageModel(MessageModel.CLUSTERING);
            // 注册消息监听器
            consumer.registerMessageListener(getMessageListener());
            // 启动消费者
            consumer.start();
            System.out.println("consumer started.%n");
        } catch (MQClientException e) {
            System.out.println(ExceptionUtils.getStackTrace(e));
//        } finally {
//            consumer.shutdown();
        }
    }

    private static MessageListenerConcurrently getMessageListener() {
        // 创建一个MessageListenerConcurrently对象
        MessageListenerConcurrently messageListenerConcurrently = new MessageListenerConcurrently() {
            @Override
            /**
             * 消息消费回调方法，异步消费消息
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
                        // 创建一个字符串缓冲区，用于存储收到的消息信息
                        StringBuffer stringBuffer = new StringBuffer("收到消息：");
                        // 将消息ID追加到字符串缓冲区
                        stringBuffer.append("===msgId: ").append(msg.getMsgId()).append("===")
                                // 将消息存储时间与出生时间之差追加到字符串缓冲区
                                .append(msg.getStoreTimestamp() - msg.getBornTimestamp()).append("ms later.[")
                                // 将消息主题追加到字符串缓冲区
                                .append("topic: ").append(topic)
                                // 将消息标签追加到字符串缓冲区
                                .append("===tags: ").append(tags)
                                // 将消息体内容追加到字符串缓冲区
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
        // 返回MessageListenerConcurrently对象
        return messageListenerConcurrently;
    }
}
