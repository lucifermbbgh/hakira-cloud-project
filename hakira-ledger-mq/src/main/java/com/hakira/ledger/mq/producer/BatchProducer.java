package com.hakira.ledger.mq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * 批量消息生产者
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class BatchProducer extends AbstractMqProducer {

    public BatchProducer(DefaultMQProducer producer) {
        super(producer);
    }
}
