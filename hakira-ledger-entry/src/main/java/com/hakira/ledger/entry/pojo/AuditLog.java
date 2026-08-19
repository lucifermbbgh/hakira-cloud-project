package com.hakira.ledger.entry.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志实体（对应 audit_log 表）
 *
 * @author hakiraKafka
 */
@Data
public class AuditLog {
    private Long logId;
    /** 操作类型 */
    private String operation;
    /** 操作人 */
    private String operator;
    /** 实体类型 */
    private String entityType;
    /** 实体 ID */
    private String entityId;
    /** 详情 */
    private String detail;
    /** 操作时间 */
    private LocalDateTime createTime;
}
