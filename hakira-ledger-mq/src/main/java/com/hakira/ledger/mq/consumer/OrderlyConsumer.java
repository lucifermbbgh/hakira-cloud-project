package com.hakira.ledger.mq.consumer;

import com.hakira.ledger.mq.config.RocketMQProperties;
import org.springframework.stereotype.Component;

/**
 * 顺序消费
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class OrderlyConsumer extends AbstractMqConsumer {

    public OrderlyConsumer(RocketMQProperties properties) {
        super(properties.getConsumerGroup(), properties);
        useClusterMode();
        setConsumeFromWhere(org.apache.rocketmq.common.consumer.ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    }
}
