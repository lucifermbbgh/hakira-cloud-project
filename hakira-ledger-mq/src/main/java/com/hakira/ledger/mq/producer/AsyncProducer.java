package com.hakira.ledger.mq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * 异步消息生产者
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class AsyncProducer extends AbstractMqProducer {

    public AsyncProducer(DefaultMQProducer producer) {
        super(producer);
    }
}
