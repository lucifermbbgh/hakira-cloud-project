package com.hakira.ledger.mq.consumer;

import com.hakira.ledger.mq.config.RocketMQProperties;
import org.springframework.stereotype.Component;

/**
 * 批量消费
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class BatchConsumer extends AbstractMqConsumer {

    public BatchConsumer(RocketMQProperties properties) {
        super("hakira-ledger-entry-batch-consumer", properties);
        useClusterMode();
    }
}
