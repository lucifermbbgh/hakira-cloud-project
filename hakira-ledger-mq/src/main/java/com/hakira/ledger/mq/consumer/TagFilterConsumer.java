package com.hakira.ledger.mq.consumer;

import com.hakira.ledger.mq.config.RocketMQProperties;
import org.springframework.stereotype.Component;

/**
 * Tag 过滤消费
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class TagFilterConsumer extends AbstractMqConsumer {

    public TagFilterConsumer(RocketMQProperties properties) {
        super("hakira-ledger-entry-tagFilter-consumer", properties);
        useClusterMode();
    }
}
