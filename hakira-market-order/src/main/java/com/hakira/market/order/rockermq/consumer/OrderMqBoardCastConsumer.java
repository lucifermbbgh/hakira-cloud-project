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
 * @Description: 广播消费者
 * @Version: 1.0
 */
public class OrderMqBoardCastConsumer {
    public static void main(String[] args) {
        // 创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hakira-market-order-consumer");
        // 指定NameServer地址
        consumer.setNamesrvAddr("127.0.0.1:9876");
        try {
            // 订阅消息
            consumer.subscribe("hakira-market-order-topic", "*");
            // 广播消息
            consumer.setMessageModel(MessageModel.BROADCASTING);
            // 注册消息监听
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

    /**
     * @description: 消息监听
     * @author: hakiraKafka
     * @date: 2023/12/12 18:14
     * @return: org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently
     **/
    private static MessageListenerConcurrently getMessageListener() {
        // 定义一个实现MessageListenerConcurrently接口的匿名内部类
        MessageListenerConcurrently messageListenerConcurrently = new MessageListenerConcurrently() {
            @Override
            // 重写consumeMessage方法，该方法用于消费消息
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    // 遍历消息列表
                    for (MessageExt msg : msgs) {
                        // 获取消息的主题
                        String topic = msg.getTopic();
                        // 获取消息的标签
                        String tags = msg.getTags();
                        // 将消息的体解析为UTF-8编码的字符串
                        String msgBody = new String(msg.getBody(), "utf-8");
                        // 创建一个StringBuffer对象，用于保存接收到的消息信息
                        StringBuffer stringBuffer = new StringBuffer("收到消息：");
                        // 将消息的主题添加到StringBuffer对象中
                        stringBuffer.append("topic: ").append(topic)
                                // 将消息的标签添加到StringBuffer对象中
                                .append("===tags: ").append(tags)
                                // 将消息的体添加到StringBuffer对象中
                                .append("===msgBody: ").append(msgBody);
                        // 打印StringBuffer对象的内容
                        System.out.println(stringBuffer.toString());
                    }
                    // 返回消费成功状态
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (UnsupportedEncodingException e) {
                    // 如果发生异常，打印异常信息
                    System.out.println(ExceptionUtils.getStackTrace(e));
                    // 返回稍后重新消费状态
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        };
        // 返回messageListenerConcurrently对象
        return messageListenerConcurrently;
    }
}
