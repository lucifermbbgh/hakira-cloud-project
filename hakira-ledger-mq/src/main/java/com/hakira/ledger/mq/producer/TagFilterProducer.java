package com.hakira.ledger.mq.producer;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.stereotype.Component;

/**
 * Tag 过滤消息生产者
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Component
public class TagFilterProducer extends AbstractMqProducer {

    public TagFilterProducer(DefaultMQProducer producer) {
        super(producer);
    }
}
