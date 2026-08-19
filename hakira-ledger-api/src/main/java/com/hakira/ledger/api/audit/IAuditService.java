package com.hakira.ledger.api.audit;

import com.hakira.ledger.api.dto.audit.AuditLogResponse;
import com.hakira.ledger.api.dto.audit.AuditTraceResponse;

import java.util.List;

/**
 * 审计服务接口 — 操作审计日志 / 数据全链路追溯
 */
public interface IAuditService {
    /** 记录审计日志 */
    void record(String operation, String entityType, String entityId, String detail);

    /** 查询最近审计日志 */
    List<AuditLogResponse> listLogs(int limit);

    /** 数据全链路追溯 */
    AuditTraceResponse trace(String entryId);
}
