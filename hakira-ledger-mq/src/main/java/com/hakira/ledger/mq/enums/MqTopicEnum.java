package com.hakira.ledger.mq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MQ Topic 枚举
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum MqTopicEnum {

    /** 账务分录事件 */
    LEDGER_ENTRY("hakira-ledger-entry-topic", "账务分录事件"),

    /** 批量账务分录 */
    LEDGER_ENTRY_BATCH("hakira-ledger-entry-batch-topic", "批量账务分录"),

    /** Tag 过滤测试 */
    LEDGER_ENTRY_TAG_FILTER("hakira-ledger-entry-tagFilter-topic", "Tag过滤测试"),

    /** 库存变动事件 */
    LEDGER_STOCK("hakira-ledger-stock-topic", "库存变动事件"),

    /** 双向核对 */
    LEDGER_RECONCILE("hakira-ledger-reconcile-topic", "双向核对"),

    /** 报表生成 */
    LEDGER_REPORT("hakira-ledger-report-topic", "报表生成"),

    /** 合规预警 */
    LEDGER_ALERT("hakira-ledger-alert-topic", "合规预警");

    /** Topic 名称 */
    private final String topic;

    /** Topic 描述 */
    private final String description;
}
