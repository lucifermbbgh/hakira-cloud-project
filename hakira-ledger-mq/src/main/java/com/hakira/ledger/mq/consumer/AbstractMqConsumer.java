package com.hakira.ledger.mq.consumer;

import com.hakira.ledger.mq.config.RocketMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * 抽象消息消费者基类
 * 提供订阅、注册监听器、生命周期管理等模板方法
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Slf4j
public abstract class AbstractMqConsumer {

    protected final DefaultMQPushConsumer consumer;
    protected final RocketMQProperties properties;

    public AbstractMqConsumer(String consumerGroup, RocketMQProperties properties) {
        this.properties = properties;
        this.consumer = new DefaultMQPushConsumer(consumerGroup);
        configureConsumer();
    }

    /**
     * 配置消费者基础参数
     */
    private void configureConsumer() {
        consumer.setNamesrvAddr(properties.getNameServer());
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setConsumeThreadMin(properties.getConsumeThreadMin());
        consumer.setConsumeThreadMax(properties.getConsumeThreadMax());
        consumer.setPullBatchSize(properties.getPullBatchSize());
        consumer.setMaxReconsumeTimes(properties.getMaxReconsumeTimes());
        consumer.setConsumeTimeout(properties.getConsumeTimeout());
    }

    // ==================== 消费模式 ====================

    /**
     * 设置为集群消费模式（默认）
     */
    public void useClusterMode() {
        consumer.setMessageModel(MessageModel.CLUSTERING);
    }

    /**
     * 设置为广播消费模式
     */
    public void useBroadcastMode() {
        consumer.setMessageModel(MessageModel.BROADCASTING);
    }

    /**
     * 设置消费起始位置
     */
    public void setConsumeFromWhere(ConsumeFromWhere fromWhere) {
        consumer.setConsumeFromWhere(fromWhere);
    }

    // ==================== 订阅 ====================

    /**
     * 订阅 Topic，使用 "*" 过滤所有 Tag
     */
    public void subscribe(String topic) {
        try {
            consumer.subscribe(topic, "*");
            log.info("消费者订阅成功: topic={}, consumerGroup={}", topic, consumer.getConsumerGroup());
        } catch (MQClientException e) {
            log.error("消费者订阅失败: topic={}, error={}", topic, ExceptionUtils.getStackTrace(e));
            throw new MqConsumerException("消费者订阅失败", e);
        }
    }

    /**
     * 订阅 Topic，指定 Tag
     */
    public void subscribe(String topic, String subExpression) {
        try {
            consumer.subscribe(topic, subExpression);
            log.info("消费者订阅成功: topic={}, subExpression={}, consumerGroup={}", topic, subExpression, consumer.getConsumerGroup());
        } catch (MQClientException e) {
            log.error("消费者订阅失败: topic={}, subExpression={}, error={}", topic, subExpression, ExceptionUtils.getStackTrace(e));
            throw new MqConsumerException("消费者订阅失败", e);
        }
    }

    /**
     * 订阅 Topic，使用 MessageSelector
     */
    public void subscribe(String topic, MessageSelector selector) {
        try {
            consumer.subscribe(topic, selector);
            log.info("消费者订阅成功: topic={}, selector={}, consumerGroup={}", topic, selector, consumer.getConsumerGroup());
        } catch (MQClientException e) {
            log.error("消费者订阅失败: topic={}, selector={}, error={}", topic, selector, ExceptionUtils.getStackTrace(e));
            throw new MqConsumerException("消费者订阅失败", e);
        }
    }

    // ==================== 注册监听器 ====================

    /**
     * 注册消息监听器
     */
    public void registerMessageListener(MessageListener listener) {
        consumer.registerMessageListener(listener);
    }

    // ==================== 生命周期 ====================

    /**
     * 启动消费者
     */
    public void start() {
        try {
            consumer.start();
            log.info("消费者启动成功: consumerGroup={}", consumer.getConsumerGroup());
        } catch (MQClientException e) {
            log.error("消费者启动失败: consumerGroup={}, error={}", consumer.getConsumerGroup(), ExceptionUtils.getStackTrace(e));
            throw new MqConsumerException("消费者启动失败", e);
        }
    }

    /**
     * 关闭消费者
     */
    public void shutdown() {
        consumer.shutdown();
        log.info("消费者已关闭: consumerGroup={}", consumer.getConsumerGroup());
    }

    /**
     * 获取原生消费者实例
     */
    public DefaultMQPushConsumer getNativeConsumer() {
        return consumer;
    }
}
