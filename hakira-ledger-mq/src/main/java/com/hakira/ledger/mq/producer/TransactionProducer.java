package com.hakira.ledger.mq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * 事务消息生产者
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class TransactionProducer extends AbstractMqProducer {

    public TransactionProducer(DefaultMQProducer producer) {
        super(producer);
    }
}
