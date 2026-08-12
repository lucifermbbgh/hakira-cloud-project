package com.hakira.ledger.entry.rockermq.producer.splitter;

import lombok.Data;
import org.apache.rocketmq.common.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.entry.rockermq.producer.spliter
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-12  17:18:45
 * @Description: 消息列表拆分器
 * @Version: 1.0
 */
@Data
public class MessageListSplitter implements Iterator<List<Message>> {
    private final int msgSizeLimit = 1024 * 1024; // 1M
    private int currentIndex;// 当前消息的下标
    private final List<Message> messageList;// 消息列表

    @Override
    public boolean hasNext() {
        // 判断是否还有下一个元素
        return currentIndex < messageList.size();
    }

    @Override
    public List<Message> next() {
        int nextIndex = currentIndex;// 下一个元素的下标
        int totalSize = 0;// 初始化当前消息的总大小
        for (; nextIndex < messageList.size(); nextIndex++) {
            // 获取下一个元素
            int tmpSize = getTmpSize(nextIndex);
            if (tmpSize > msgSizeLimit) {
                // 单个消息超过了最大的限制
                if (nextIndex - currentIndex == 0) {
                    // 如果当前的消息都超过了最大的限制(1M)，否则会阻塞进程
                    nextIndex++;// 假如下一个子列表没有元素，则添加这个子列表然后退出循环，否则退出循环
                }
                break;
            }
            if (tmpSize + totalSize > msgSizeLimit) {// 如果当前的消息超过了最大的限制(1M)
                break;// 退出循环
            } else {
                totalSize += tmpSize;// 累加当前消息的大小
            }
        }

        List<Message> subList = messageList.subList(currentIndex, nextIndex);// 截取子列表
        currentIndex = nextIndex;// 更新当前的下标
        return subList;// 返回子列表
    }

    /**
     * @description: 获取下一个消息的大小
     * @author: hakiraKafka
     * @date: 2023/12/12 17:38
     * @param: nextIndex
     * @return: int
     **/
    private int getTmpSize(int nextIndex) {
        // 获取下一个消息
        Message message = messageList.get(nextIndex);
        // 获取消息的大小
        int tmpSize = message.getTopic().length() + message.getBody().length;
        // 获取消息的属性
        Map<String, String> properties = message.getProperties();
        // 遍历属性
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            tmpSize += entry.getKey().length() + entry.getValue().length();
        }
        // 增加日志的开销20字节
        tmpSize += 20;
        return tmpSize;
    }
}
