package com.hakira.ledger.api.dto.audit;

import lombok.Data;

/**
 * 审计日志响应
 */
@Data
public class AuditLogResponse {
    private Long logId;
    private String operation;
    private String operator;
    private String entityType;
    private String entityId;
    private String detail;
    private String createTime;
}
