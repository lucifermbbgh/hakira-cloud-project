package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.audit.IAuditService;
import com.hakira.ledger.api.dto.audit.AuditLogResponse;
import com.hakira.ledger.api.dto.audit.AuditTraceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 审计控制器
 *
 * @author hakiraKafka
 */
@RestController
@RequestMapping("/audit")
@Slf4j
@RequiredArgsConstructor
public class AuditController {

    private final IAuditService auditService;

    @GetMapping("/logs")
    public Result<List<AuditLogResponse>> logs(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        return Result.returnSuccess(auditService.listLogs(limit));
    }

    @GetMapping("/trace/{entryId}")
    public Result<AuditTraceResponse> trace(@PathVariable("entryId") String entryId) {
        return Result.returnSuccess(auditService.trace(entryId));
    }
}
