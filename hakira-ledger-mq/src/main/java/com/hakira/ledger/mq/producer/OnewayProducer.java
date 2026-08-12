package com.hakira.ledger.mq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * 单向消息生产者
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class OnewayProducer extends AbstractMqProducer {

    public OnewayProducer(DefaultMQProducer producer) {
        super(producer);
    }
}
