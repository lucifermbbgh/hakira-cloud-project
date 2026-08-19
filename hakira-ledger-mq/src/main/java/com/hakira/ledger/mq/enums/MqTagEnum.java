package com.hakira.ledger.mq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MQ Tag 枚举
 *
 * @author hakiraKafka
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum MqTagEnum {

    /** 默认标签 */
    DEFAULT_TAG("hakira-ledger-entry-tag", "默认标签"),

    /** 标签A */
    TAG_A("tagA", "标签A"),

    /** 标签B */
    TAG_B("tagB", "标签B"),

    /** 标签C */
    TAG_C("tagC", "标签C"),

    /** 批量标签 */
    BATCH_TAG("hakira-ledger-entry-batch-tag", "批量标签"),

    /** Tag 过滤标签 */
    TAG_FILTER("hakira-ledger-entry-tagFilter-tag", "Tag过滤标签");

    /** Tag 名称 */
    private final String tag;

    /** Tag 描述 */
    private final String description;
}
