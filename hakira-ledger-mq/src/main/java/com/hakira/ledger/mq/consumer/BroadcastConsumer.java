package com.hakira.ledger.mq.consumer;

import com.hakira.ledger.mq.config.RocketMQProperties;
import org.springframework.stereotype.Component;

/**
 * 广播消费
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class BroadcastConsumer extends AbstractMqConsumer {

    public BroadcastConsumer(RocketMQProperties properties) {
        super(properties.getConsumerGroup(), properties);
        useBroadcastMode();
    }
}
