package com.hakira.ledger.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;

import java.util.List;

/**
 * 抽象消息生产者基类
 * 提供同步/异步/单向/延迟/顺序/批量等模板方法
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Slf4j
public abstract class AbstractMqProducer {

    protected final DefaultMQProducer producer;

    public AbstractMqProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    // ==================== 同步发送 ====================

    /**
     * 同步发送消息
     */
    public SendResult sendSync(Message message) {
        try {
            SendResult result = producer.send(message);
            log.debug("同步消息发送成功: msgId={}, topic={}, tags={}", result.getMsgId(), message.getTopic(), message.getTags());
            return result;
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("同步消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("同步消息发送失败", e);
        }
    }

    /**
     * 同步发送消息，指定超时时间
     */
    public SendResult sendSync(Message message, long timeout) {
        try {
            return producer.send(message, timeout);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("同步消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("同步消息发送失败", e);
        }
    }

    /**
     * 同步发送消息，指定队列选择器
     */
    public SendResult sendSync(Message message, MessageQueueSelector selector, Object arg) {
        try {
            return producer.send(message, selector, arg);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("同步消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("同步消息发送失败", e);
        }
    }

    /**
     * 同步发送消息，直接指定消息队列
     */
    public SendResult sendSync(Message message, MessageQueue messageQueue) {
        try {
            return producer.send(message, messageQueue);
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("同步消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("同步消息发送失败", e);
        }
    }

    // ==================== 异步发送 ====================

    /**
     * 异步发送消息
     */
    public void sendAsync(Message message, SendCallback callback) {
        try {
            producer.send(message, callback);
        } catch (MQClientException | RemotingException | InterruptedException e) {
            log.error("异步消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("异步消息发送失败", e);
        }
    }

    /**
     * 异步发送消息，指定超时
     */
    public void sendAsync(Message message, SendCallback callback, long timeout) {
        try {
            producer.send(message, callback, timeout);
        } catch (MQClientException | RemotingException | InterruptedException e) {
            log.error("异步消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("异步消息发送失败", e);
        }
    }

    /**
     * 异步发送消息，指定队列选择器
     */
    public void sendAsync(Message message, MessageQueueSelector selector, Object arg, SendCallback callback) {
        try {
            producer.send(message, selector, arg, callback);
        } catch (MQClientException | RemotingException | InterruptedException e) {
            log.error("异步消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("异步消息发送失败", e);
        }
    }

    // ==================== 单向发送 ====================

    /**
     * 单向发送消息（不关心结果）
     */
    public void sendOneway(Message message) {
        try {
            producer.sendOneway(message);
            log.debug("单向消息发送成功: topic={}, tags={}", message.getTopic(), message.getTags());
        } catch (MQClientException | RemotingException | InterruptedException e) {
            log.error("单向消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("单向消息发送失败", e);
        }
    }

    /**
     * 单向发送消息，指定队列选择器
     */
    public void sendOneway(Message message, MessageQueueSelector selector, Object arg) {
        try {
            producer.sendOneway(message, selector, arg);
        } catch (MQClientException | RemotingException | InterruptedException e) {
            log.error("单向消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("单向消息发送失败", e);
        }
    }

    /**
     * 单向发送消息，直接指定消息队列
     */
    public void sendOneway(Message message, MessageQueue messageQueue) {
        try {
            producer.sendOneway(message, messageQueue);
        } catch (MQClientException | RemotingException | InterruptedException e) {
            log.error("单向消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("单向消息发送失败", e);
        }
    }

    // ==================== 延迟发送 ====================

    /**
     * 发送延迟消息
     * 延迟等级: 1=1s, 2=5s, 3=10s, 4=30s, 5=1m, 6=2m, 7=3m, 8=4m, 9=5m,
     *           10=6m, 11=7m, 12=8m, 13=9m, 14=10m, 15=20m, 16=30m, 17=1h, 18=2h
     */
    public SendResult sendDelay(Message message, int delayLevel) {
        message.setDelayTimeLevel(delayLevel);
        return sendSync(message);
    }

    // ==================== 顺序发送 ====================

    /**
     * 发送顺序消息
     */
    public SendResult sendOrderly(Message message, MessageQueueSelector selector, Object arg) {
        try {
            SendResult result = producer.send(message, selector, arg);
            log.debug("顺序消息发送成功: msgId={}, topic={}, tags={}", result.getMsgId(), message.getTopic(), message.getTags());
            return result;
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("顺序消息发送失败: topic={}, tags={}, error={}", message.getTopic(), message.getTags(), ExceptionUtils.getStackTrace(e));
            throw new MqException("顺序消息发送失败", e);
        }
    }

    // ==================== 批量发送 ====================

    /**
     * 批量发送消息
     */
    public SendResult sendBatch(List<Message> messages) {
        try {
            SendResult result = producer.send(messages);
            log.debug("批量消息发送成功: msgId={}, count={}", result.getMsgId(), messages.size());
            return result;
        } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
            log.error("批量消息发送失败: count={}, error={}", messages.size(), ExceptionUtils.getStackTrace(e));
            throw new MqException("批量消息发送失败", e);
        }
    }

    // ==================== 事务消息 ====================

    /**
     * 发送事务消息
     */
    public TransactionSendResult sendTransaction(Message message, Object arg) {
        try {
            TransactionSendResult result = producer.sendMessageInTransaction(message, arg);
            log.debug("事务消息发送成功: msgId={}, topic={}", result.getMsgId(), message.getTopic());
            return result;
        } catch (MQClientException e) {
            log.error("事务消息发送失败: topic={}, error={}", message.getTopic(), ExceptionUtils.getStackTrace(e));
            throw new MqException("事务消息发送失败", e);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 获取 Topic 下的消息队列列表
     */
    public List<MessageQueue> fetchPublishMessageQueues(String topic) {
        try {
            return producer.fetchPublishMessageQueues(topic);
        } catch (MQClientException e) {
            log.error("获取消息队列失败: topic={}, error={}", topic, ExceptionUtils.getStackTrace(e));
            throw new MqException("获取消息队列失败", e);
        }
    }
}
