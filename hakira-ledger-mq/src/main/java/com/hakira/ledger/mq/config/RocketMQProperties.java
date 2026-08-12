package com.hakira.ledger.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RocketMQ 配置属性
 * 前缀: hakira.mq
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Data
@ConfigurationProperties(prefix = "hakira.mq")
public class RocketMQProperties {

    /** NameServer 地址，默认 127.0.0.1:9876 */
    private String nameServer = "127.0.0.1:9876";

    /** 生产者组名 */
    private String producerGroup = "hakira-ledger-producer";

    /** 消费者组名 */
    private String consumerGroup = "hakira-ledger-consumer";

    /** 发送消息超时时间，单位 ms，默认 3000ms */
    private int sendMsgTimeout = 3000;

    /** 同步发送失败重试次数，默认 3 */
    private int retryTimes = 3;

    /** 异步发送失败重试次数，默认 3 */
    private int retryTimesAsync = 3;

    /** 消息最大长度，默认 10MB */
    private int maxMessageSize = 10 * 1024 * 1024;

    /** 消息压缩阈值，单位 KB，默认 10KB */
    private int compressMsgBodyOverHowmuch = 1024 * 10;

    /** 默认 Topic 队列数（仅对新 Topic 生效） */
    private int defaultTopicQueueNums = 8;

    /** 消费线程数最小值 */
    private int consumeThreadMin = 20;

    /** 消费线程数最大值 */
    private int consumeThreadMax = 20;

    /** 每次拉取消息数量 */
    private int pullBatchSize = 32;

    /** 消费超时时间，单位分钟，默认 15 */
    private int consumeTimeout = 15;

    /** 消息消费失败最大重试次数，-1 代表 16 次 */
    private int maxReconsumeTimes = -1;
}
