package com.hakira.ledger.mq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * 延迟消息生产者
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class DelayProducer extends AbstractMqProducer {

    public DelayProducer(DefaultMQProducer producer) {
        super(producer);
    }
}
