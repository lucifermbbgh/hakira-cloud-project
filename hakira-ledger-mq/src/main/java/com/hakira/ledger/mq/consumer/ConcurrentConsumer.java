package com.hakira.ledger.mq.consumer;

import com.hakira.ledger.mq.config.RocketMQProperties;
import org.springframework.stereotype.Component;

/**
 * 集群消费（并发消费/均衡消费）
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class ConcurrentConsumer extends AbstractMqConsumer {

    public ConcurrentConsumer(RocketMQProperties properties) {
        super(properties.getConsumerGroup(), properties);
        useClusterMode();
    }
}
