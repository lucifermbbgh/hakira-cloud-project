package com.hakira.ledger.mq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * 顺序消息生产者
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class OrderlyProducer extends AbstractMqProducer {

    public OrderlyProducer(DefaultMQProducer producer) {
        super(producer);
    }
}
