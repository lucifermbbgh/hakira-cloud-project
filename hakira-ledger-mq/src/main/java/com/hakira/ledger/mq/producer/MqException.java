package com.hakira.ledger.mq.producer;

/**
 * MQ 消息异常
 *
 * @author hakiraKafka
 * @version 1.0
 */
public class MqException extends RuntimeException {

    public MqException(String message) {
        super(message);
    }

    public MqException(String message, Throwable cause) {
        super(message, cause);
    }
}
