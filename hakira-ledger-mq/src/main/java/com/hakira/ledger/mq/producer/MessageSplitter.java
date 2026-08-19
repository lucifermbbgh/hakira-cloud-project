package com.hakira.ledger.mq.producer;

import lombok.Data;
import org.apache.rocketmq.common.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 消息列表拆分器 —— 将大批量消息拆分为每批不超过 1MB 的子列表
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Data
public class MessageSplitter implements Iterator<List<Message>> {

    /** 单个子列表大小上限: 1MB */
    private static final int MSG_SIZE_LIMIT = 1024 * 1024;

    /** 当前消息下标 */
    private int currentIndex;

    /** 原始消息列表 */
    private final List<Message> messageList;

    public MessageSplitter(List<Message> messageList) {
        this.messageList = messageList;
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < messageList.size();
    }

    @Override
    public List<Message> next() {
        int nextIndex = currentIndex;
        int totalSize = 0;
        for (; nextIndex < messageList.size(); nextIndex++) {
            int tmpSize = calcMessageSize(nextIndex);
            if (tmpSize > MSG_SIZE_LIMIT) {
                if (nextIndex - currentIndex == 0) {
                    nextIndex++;
                }
                break;
            }
            if (tmpSize + totalSize > MSG_SIZE_LIMIT) {
                break;
            } else {
                totalSize += tmpSize;
            }
        }
        List<Message> subList = messageList.subList(currentIndex, nextIndex);
        currentIndex = nextIndex;
        return subList;
    }

    /**
     * 计算单条消息的估算大小 (body + topic + properties + 日志开销)
     */
    private int calcMessageSize(int index) {
        Message message = messageList.get(index);
        int size = message.getTopic().length() + message.getBody().length;
        Map<String, String> properties = message.getProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            size += entry.getKey().length() + entry.getValue().length();
        }
        size += 20;
        return size;
    }
}
