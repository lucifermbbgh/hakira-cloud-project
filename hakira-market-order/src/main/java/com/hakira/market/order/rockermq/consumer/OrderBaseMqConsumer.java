package com.hakira.market.order.rockermq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.market.order.rockermq.consumer
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-11  22:26:31
 * @Description: 均衡消费者
 * @Version: 1.0
 */
@Slf4j
public class OrderBaseMqConsumer {

    public static DefaultMQPushConsumer initConsumer() {
        // 创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("hakira-market-order-consumer");
        // 指定NameServer地址
        consumer.setNamesrvAddr("127.0.0.1:9876");
        // 消息消费模式，默认集群消费
        // MessageModel.CLUSTERING集群消费
        // MessageModel.BROADCASTING广播消费
        consumer.setMessageModel(MessageModel.CLUSTERING);
        // 消息消费偏移量，指定消费开始偏移量开始消费（默认为CONSUME_FROM_LAST_OFFSET）
        // 上次消费偏移量CONSUME_FROM_LAST_OFFSET
        // 最大偏移量CONSUME_FROM_MAX_OFFSET
        // 最小偏移量CONSUME_FROM_MIN_OFFSET
        // 启动时间戳CONSUME_FROM_TIMESTAMP
        // 从队列的最前位置开始消费CONSUME_FROM_FIRST_OFFSET
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        // 设置消费最小线程数量，默认为20
        consumer.setConsumeThreadMin(20);
        // 设置消费最大线程数量，默认为20
        consumer.setConsumeThreadMax(20);
        // 推模式下，任务间隔时间（推模式：基于不断的轮询拉取的封装）
        consumer.setPullInterval(0);
        // 推模式下，每次最多拉取的消息数量，默认为32
        consumer.setPullBatchSize(32);
        // 消息消费失败时的最大重试次数，默认为-1代表16次
        consumer.setMaxReconsumeTimes(-1);
        // 设置消费超时时间，单位分钟，默认为15分钟（单位分钟）（-1代表不超时）
        consumer.setConsumeTimeout(15);
        return consumer;
    }

    public static void main(String[] args) {
        // 创建消费者
        DefaultMQPushConsumer consumer = initConsumer();
        try {
            // 订阅消息，根据正则表达式过滤
            consumer.subscribe("hakira-market-order-topic", "*");
            // 订阅消息，根据SQL表达式过滤
            consumer.subscribe("hakira-market-order-topic", MessageSelector.bySql("a between 0 and 3"));
            // 订阅消息，根据Tag过滤
            consumer.subscribe("hakira-market-order-topic", MessageSelector.byTag("tagA || tagB"));
            // 取消订阅消息
            consumer.unsubscribe("hakira-market-order-default-topic");
            // 获取消费者对主题分配了哪些消息队列
            Set<MessageQueue> messageQueues = consumer.fetchSubscribeMessageQueues("hakira-market-order-topic");

            // 注册消息监听器
            consumer.registerMessageListener(new MessageListenerConcurrently() {
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
            });

            // 注册顺序消息监听器
            consumer.registerMessageListener(new MessageListenerOrderly() {
                Random random = new Random();

                @Override
                public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                    // 设置自动提交
                    context.setAutoCommit(true);
                    for (MessageExt msg : msgs) {
                        System.out.println("consumerThread = " + Thread.currentThread().getName() +
                                "queueId = " + msg.getQueueId());
                        try {
                            // 线程睡眠1-1000毫秒，模拟业务逻辑处理耗时
                            TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
                        } catch (InterruptedException e) {
                            log.error("consumerThread = {}，发生异常，错误信息：{}", Thread.currentThread().getName(), ExceptionUtils.getStackTrace(e));
                            // 返回消息等待再消费，并非消息进入重试队列
                            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                        }
                    }
                    // 返回消费成功
                    return ConsumeOrderlyStatus.SUCCESS;
                }
            });

            // 启动消费者
            consumer.start();
            System.out.println("consumer started.%n");
        } catch (MQClientException e) {
            log.error("发送消息失败，错误信息：{}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        } finally {
            // 关闭消费者
            consumer.shutdown();
        }
    }
}
