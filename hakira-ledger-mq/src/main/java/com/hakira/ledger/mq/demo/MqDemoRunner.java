package com.hakira.ledger.mq.demo;

import com.hakira.ledger.mq.consumer.BatchConsumer;
import com.hakira.ledger.mq.consumer.BroadcastConsumer;
import com.hakira.ledger.mq.consumer.ConcurrentConsumer;
import com.hakira.ledger.mq.consumer.OrderlyConsumer;
import com.hakira.ledger.mq.consumer.TagFilterConsumer;
import com.hakira.ledger.mq.enums.MqTagEnum;
import com.hakira.ledger.mq.enums.MqTopicEnum;
import com.hakira.ledger.mq.producer.AsyncProducer;
import com.hakira.ledger.mq.producer.BatchProducer;
import com.hakira.ledger.mq.producer.DelayProducer;
import com.hakira.ledger.mq.producer.MessageSplitter;
import com.hakira.ledger.mq.producer.OnewayProducer;
import com.hakira.ledger.mq.producer.OrderlyProducer;
import com.hakira.ledger.mq.producer.SyncProducer;
import com.hakira.ledger.mq.producer.TagFilterProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * MQ Demo Runner - 演示所有消息发送/消费模式
 * 仅在 hakira.mq.demo.enabled=true 时启动，避免生产环境误执行
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "hakira.mq.demo", name = "enabled", havingValue = "true")
public class MqDemoRunner implements CommandLineRunner {

    private final SyncProducer syncProducer;
    private final AsyncProducer asyncProducer;
    private final OnewayProducer onewayProducer;
    private final DelayProducer delayProducer;
    private final OrderlyProducer orderlyProducer;
    private final BatchProducer batchProducer;
    private final TagFilterProducer tagFilterProducer;

    private final ConcurrentConsumer concurrentConsumer;
    private final BroadcastConsumer broadcastConsumer;
    private final OrderlyConsumer orderlyConsumer;
    private final BatchConsumer batchConsumer;
    private final TagFilterConsumer tagFilterConsumer;

    public MqDemoRunner(SyncProducer syncProducer, AsyncProducer asyncProducer,
                        OnewayProducer onewayProducer, DelayProducer delayProducer,
                        OrderlyProducer orderlyProducer, BatchProducer batchProducer,
                        TagFilterProducer tagFilterProducer,
                        ConcurrentConsumer concurrentConsumer,
                        BroadcastConsumer broadcastConsumer,
                        OrderlyConsumer orderlyConsumer,
                        BatchConsumer batchConsumer,
                        TagFilterConsumer tagFilterConsumer) {
        this.syncProducer = syncProducer;
        this.asyncProducer = asyncProducer;
        this.onewayProducer = onewayProducer;
        this.delayProducer = delayProducer;
        this.orderlyProducer = orderlyProducer;
        this.batchProducer = batchProducer;
        this.tagFilterProducer = tagFilterProducer;
        this.concurrentConsumer = concurrentConsumer;
        this.broadcastConsumer = broadcastConsumer;
        this.orderlyConsumer = orderlyConsumer;
        this.batchConsumer = batchConsumer;
        this.tagFilterConsumer = tagFilterConsumer;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("==================== MQ Demo 开始 ====================");

        demoConsumers();
        demoProducers();

        log.info("==================== MQ Demo 结束 ====================");
    }

    // ==================== Consumer 演示 ====================

    private void demoConsumers() throws Exception {
        log.info("---------- Consumer 演示 ----------");

        // 1. 并发消费（集群消费）
        demoConcurrentConsumer();

        // 2. 广播消费
        demoBroadcastConsumer();

        // 3. 顺序消费
        demoOrderlyConsumer();

        // 4. 批量消费
        demoBatchConsumer();

        // 5. Tag 过滤消费
        demoTagFilterConsumer();

        log.info("所有 Consumer 已启动，等待消息...");
        Thread.sleep(5000);
    }

    private void demoConcurrentConsumer() throws Exception {
        log.info(">> 并发消费（集群消费）演示");
        concurrentConsumer.subscribe(MqTopicEnum.LEDGER_ENTRY.getTopic());
        concurrentConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    for (MessageExt msg : msgs) {
                        String msgBody = new String(msg.getBody(), "utf-8");
                        log.info("[并发消费] msgId={}, topic={}, tags={}, body={}, delay={}ms",
                                msg.getMsgId(), msg.getTopic(), msg.getTags(), msgBody,
                                msg.getStoreTimestamp() - msg.getBornTimestamp());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    log.error("[并发消费] 异常: {}", ExceptionUtils.getStackTrace(e));
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        concurrentConsumer.start();
    }

    private void demoBroadcastConsumer() throws Exception {
        log.info(">> 广播消费演示");
        broadcastConsumer.subscribe(MqTopicEnum.LEDGER_ENTRY.getTopic());
        broadcastConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    for (MessageExt msg : msgs) {
                        String msgBody = new String(msg.getBody(), "utf-8");
                        log.info("[广播消费] topic={}, tags={}, body={}", msg.getTopic(), msg.getTags(), msgBody);
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    log.error("[广播消费] 异常: {}", ExceptionUtils.getStackTrace(e));
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        broadcastConsumer.start();
    }

    private void demoOrderlyConsumer() throws Exception {
        log.info(">> 顺序消费演示");
        orderlyConsumer.subscribe(MqTopicEnum.LEDGER_ENTRY.getTopic());
        orderlyConsumer.registerMessageListener(new MessageListenerOrderly() {
            final Random random = new Random();

            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                try {
                    context.setAutoCommit(true);
                    for (MessageExt msg : msgs) {
                        String msgBody = new String(msg.getBody(), "utf-8");
                        log.info("[顺序消费] thread={}, queueId={}, topic={}, tags={}, body={}",
                                Thread.currentThread().getName(), msg.getQueueId(),
                                msg.getTopic(), msg.getTags(), msgBody);
                    }
                    TimeUnit.MILLISECONDS.sleep(random.nextInt(1000));
                    return ConsumeOrderlyStatus.SUCCESS;
                } catch (Exception e) {
                    log.error("[顺序消费] 异常: {}", ExceptionUtils.getStackTrace(e));
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            }
        });
        orderlyConsumer.start();
    }

    private void demoBatchConsumer() throws Exception {
        log.info(">> 批量消费演示");
        batchConsumer.subscribe(MqTopicEnum.LEDGER_ENTRY_BATCH.getTopic());
        batchConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    log.info("[批量消费] thread={}, 收到{}条消息: {}", Thread.currentThread().getName(), msgs.size(), msgs);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    log.error("[批量消费] 异常: {}", ExceptionUtils.getStackTrace(e));
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        batchConsumer.start();
    }

    private void demoTagFilterConsumer() throws Exception {
        log.info(">> Tag 过滤消费演示");
        String tagsFilterSql = "TAGS is not null and TAGS in ('tagA','tagC') and (a is not null and a between 0 and 3)";
        MessageSelector messageSelector = MessageSelector.bySql(tagsFilterSql);
        tagFilterConsumer.subscribe(MqTopicEnum.LEDGER_ENTRY_TAG_FILTER.getTopic(), messageSelector);
        tagFilterConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                try {
                    for (MessageExt msg : msgs) {
                        String msgBody = new String(msg.getBody(), "utf-8");
                        String property = msg.getProperty("a");
                        log.info("[Tag过滤消费] topic={}, tags={}, a={}, body={}",
                                msg.getTopic(), msg.getTags(), property, msgBody);
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    log.error("[Tag过滤消费] 异常: {}", ExceptionUtils.getStackTrace(e));
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            }
        });
        tagFilterConsumer.start();
    }

    // ==================== Producer 演示 ====================

    private void demoProducers() throws Exception {
        log.info("---------- Producer 演示 ----------");

        demoSyncSend();
        demoAsyncSend();
        demoOnewaySend();
        demoDelaySend();
        demoOrderlySend();
        demoBatchSend();
        demoTagFilterSend();

        // 等待异步消息发送完毕
        Thread.sleep(3000);
    }

    private void demoSyncSend() throws Exception {
        log.info(">> 同步消息发送演示");
        for (int i = 0; i < 10; i++) {
            Message message = new Message(
                    MqTopicEnum.LEDGER_ENTRY.getTopic(),
                    MqTagEnum.DEFAULT_TAG.getTag(),
                    "sync-key-" + i,
                    ("sync-body-" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult result = syncProducer.sendSync(message);
            log.info("[同步发送] msgId={}, index={}", result.getMsgId(), i);
        }
    }

    private void demoAsyncSend() throws Exception {
        log.info(">> 异步消息发送演示");
        for (int i = 0; i < 10; i++) {
            final int index = i;
            Message message = new Message(
                    MqTopicEnum.LEDGER_ENTRY.getTopic(),
                    MqTagEnum.DEFAULT_TAG.getTag(),
                    "async-key-" + i,
                    ("async-body-" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            asyncProducer.sendAsync(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("[异步发送] index={}, msgId={}", index, sendResult.getMsgId());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("[异步发送] index={}, error: {}", index, ExceptionUtils.getStackTrace(e));
                }
            });
        }
    }

    private void demoOnewaySend() throws Exception {
        log.info(">> 单向消息发送演示");
        for (int i = 0; i < 10; i++) {
            Message message = new Message(
                    MqTopicEnum.LEDGER_ENTRY.getTopic(),
                    MqTagEnum.DEFAULT_TAG.getTag(),
                    "oneway-key-" + i,
                    ("oneway-body-" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            onewayProducer.sendOneway(message);
            log.info("[单向发送] index={}", i);
        }
    }

    private void demoDelaySend() throws Exception {
        log.info(">> 延迟消息发送演示（延迟10秒）");
        for (int i = 0; i < 5; i++) {
            Message message = new Message(
                    MqTopicEnum.LEDGER_ENTRY.getTopic(),
                    MqTagEnum.DEFAULT_TAG.getTag(),
                    "delay-key-" + i,
                    ("delay-body-" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            // delayLevel=3 表示延迟 10 秒
            SendResult result = delayProducer.sendDelay(message, 3);
            log.info("[延迟发送] msgId={}, delayLevel=3(10s)", result.getMsgId());
        }
    }

    private void demoOrderlySend() throws Exception {
        log.info(">> 顺序消息发送演示");
        for (int i = 0; i < 16; i++) {
            final String id = String.valueOf(i % 4 + 1);
            Message message = new Message(
                    MqTopicEnum.LEDGER_ENTRY.getTopic(),
                    MqTagEnum.DEFAULT_TAG.getTag(),
                    "orderly-key-" + i,
                    ("orderly-body-" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult result = orderlyProducer.sendOrderly(message, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    long idx = Long.parseLong(arg.toString()) % mqs.size();
                    return mqs.get((int) idx);
                }
            }, id);
            log.info("[顺序发送] msgId={}, id={}", result.getMsgId(), id);
        }
    }

    private void demoBatchSend() throws Exception {
        log.info(">> 批量消息发送演示（1万条消息，使用拆分器分批发送）");
        List<Message> messageList = new ArrayList<>(10000);
        for (int i = 0; i < 10000; i++) {
            Message message = new Message(
                    MqTopicEnum.LEDGER_ENTRY_BATCH.getTopic(),
                    MqTagEnum.BATCH_TAG.getTag(),
                    "batch-key-" + i,
                    ("batch-body-" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            messageList.add(message);
        }

        MessageSplitter splitter = new MessageSplitter(messageList);
        int batchNo = 0;
        while (splitter.hasNext()) {
            List<Message> batch = splitter.next();
            SendResult result = batchProducer.sendBatch(batch);
            log.info("[批量发送] 第{}批, 数量={}, msgId={}", ++batchNo, batch.size(), result.getMsgId());
            Thread.sleep(500);
        }
    }

    private void demoTagFilterSend() throws Exception {
        log.info(">> Tag 过滤消息发送演示（发送 tagA/tagB/tagC 三种标签，属性 a 递增）");
        String[] tags = {MqTagEnum.TAG_A.getTag(), MqTagEnum.TAG_B.getTag(), MqTagEnum.TAG_C.getTag()};
        for (int i = 0; i < 10; i++) {
            Message message = new Message(
                    MqTopicEnum.LEDGER_ENTRY_TAG_FILTER.getTopic(),
                    tags[i % tags.length],
                    "tagFilter-key-" + i,
                    ("tagFilter-body-" + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
            message.putUserProperty("a", String.valueOf(i));
            SendResult result = tagFilterProducer.sendSync(message);
            log.info("[Tag过滤发送] msgId={}, tag={}, a={}", result.getMsgId(), tags[i % tags.length], i);
        }
    }
}
