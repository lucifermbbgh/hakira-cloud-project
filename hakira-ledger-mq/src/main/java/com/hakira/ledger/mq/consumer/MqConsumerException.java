package com.hakira.ledger.mq.consumer;

/**
 * MQ 消费者异常
 *
 * @author hakiraKafka
 * @version 1.0
 */
public class MqConsumerException extends RuntimeException {

    public MqConsumerException(String message) {
        super(message);
    }

    public MqConsumerException(String message, Throwable cause) {
        super(message, cause);
    }
}
