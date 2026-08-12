package com.hakira.ledger.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;

/**
 * RocketMQ 自动配置类
 * 创建 DefaultMQProducer Bean，并在容器销毁时关闭
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RocketMQProperties.class)
public class RocketMQConfig {

    /**
     * 创建 DefaultMQProducer Bean
     * 当应用上下文中不存在同类型 Bean 时自动创建
     */
    @Bean
    @ConditionalOnMissingBean(DefaultMQProducer.class)
    public DefaultMQProducer defaultMQProducer(RocketMQProperties props) {
        DefaultMQProducer producer = new DefaultMQProducer(props.getProducerGroup());
        producer.setNamesrvAddr(props.getNameServer());
        producer.setDefaultTopicQueueNums(props.getDefaultTopicQueueNums());
        producer.setSendMsgTimeout(props.getSendMsgTimeout());
        producer.setCompressMsgBodyOverHowmuch(props.getCompressMsgBodyOverHowmuch());
        producer.setRetryTimesWhenSendFailed(props.getRetryTimes());
        producer.setRetryTimesWhenSendAsyncFailed(props.getRetryTimesAsync());
        producer.setRetryAnotherBrokerWhenNotStoreOK(true);
        producer.setMaxMessageSize(props.getMaxMessageSize());

        try {
            producer.start();
            log.info("RocketMQ Producer 启动成功, nameServer={}, producerGroup={}",
                    props.getNameServer(), props.getProducerGroup());
        } catch (MQClientException e) {
            log.error("RocketMQ Producer 启动失败: {}", ExceptionUtils.getStackTrace(e));
            throw new RuntimeException("RocketMQ Producer 启动失败", e);
        }
        return producer;
    }

    /**
     * Bean 销毁时关闭 Producer
     */
    @PreDestroy
    public void shutdownProducer(DefaultMQProducer producer) {
        if (producer != null) {
            producer.shutdown();
            log.info("RocketMQ Producer 已关闭");
        }
    }
}
