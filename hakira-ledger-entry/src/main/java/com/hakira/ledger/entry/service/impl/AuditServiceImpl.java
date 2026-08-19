package com.hakira.ledger.entry.service.impl;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.ledger.api.audit.IAuditService;
import com.hakira.ledger.api.dto.audit.AuditLogResponse;
import com.hakira.ledger.api.dto.audit.AuditTraceResponse;
import com.hakira.ledger.entry.mapper.AuditLogMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineAuxMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineMapper;
import com.hakira.ledger.entry.mapper.JournalEntryMapper;
import com.hakira.ledger.entry.pojo.AuditLog;
import com.hakira.ledger.entry.pojo.AuditMovement;
import com.hakira.ledger.entry.pojo.JournalEntry;
import com.hakira.ledger.entry.pojo.JournalEntryLine;
import com.hakira.ledger.entry.pojo.JournalEntryLineAux;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审计服务实现（操作审计日志 / 数据全链路追溯）
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuditServiceImpl implements IAuditService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditLogMapper auditLogMapper;
    private final JournalEntryMapper journalEntryMapper;
    private final JournalEntryLineMapper journalEntryLineMapper;
    private final JournalEntryLineAuxMapper journalEntryLineAuxMapper;

    @Override
    public void record(String operation, String entityType, String entityId, String detail) {
        AuditLog log = new AuditLog();
        log.setOperation(operation);
        log.setOperator("system");
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetail(detail);
        auditLogMapper.insert(log);
    }

    @Override
    public List<AuditLogResponse> listLogs(int limit) {
        return auditLogMapper.selectRecent(limit).stream().map(log -> {
            AuditLogResponse r = new AuditLogResponse();
            r.setLogId(log.getLogId());
            r.setOperation(log.getOperation());
            r.setOperator(log.getOperator());
            r.setEntityType(log.getEntityType());
            r.setEntityId(log.getEntityId());
            r.setDetail(log.getDetail());
            r.setCreateTime(log.getCreateTime() != null ? log.getCreateTime().format(DATETIME_FORMATTER) : null);
            return r;
        }).collect(Collectors.toList());
    }

    @Override
    public AuditTraceResponse trace(String entryId) {
        JournalEntry entry = journalEntryMapper.selectByEntryId(entryId);
        if (entry == null) {
            throw new BizException(BizErrorCode.ENTRY_NOT_FOUND, entryId);
        }
        List<JournalEntryLine> lines = journalEntryLineMapper.selectByEntryId(entryId, entry.getEntryDate());
        Map<Long, List<JournalEntryLineAux>> auxMap = journalEntryLineAuxMapper
                .selectByEntryId(entryId, entry.getEntryDate())
                .stream()
                .collect(Collectors.groupingBy(JournalEntryLineAux::getLineId));

        AuditTraceResponse response = new AuditTraceResponse();
        response.setEntryId(entry.getEntryId());
        response.setVoucherNo(entry.getVoucherNo());
        response.setEntryDate(entry.getEntryDate() != null ? entry.getEntryDate().toString() : null);
        response.setDescription(entry.getDescription());
        response.setStatus(entry.getStatus());
        response.setTotalDebit(entry.getTotalDebit());
        response.setTotalCredit(entry.getTotalCredit());

        response.setLines(lines.stream().map(l -> {
            AuditTraceResponse.Line lr = new AuditTraceResponse.Line();
            lr.setLineId(l.getLineId());
            lr.setSubjectCode(l.getSubjectCode());
            lr.setSubjectName(l.getSubjectName());
            lr.setDebitAmount(l.getDebitAmount());
            lr.setCreditAmount(l.getCreditAmount());
            List<JournalEntryLineAux> auxList = auxMap.get(l.getLineId());
            if (auxList != null) {
                lr.setAux(auxList.stream().map(a -> {
                    AuditTraceResponse.Aux aux = new AuditTraceResponse.Aux();
                    aux.setDimensionCode(a.getDimensionCode());
                    aux.setValueCode(a.getValueCode());
                    return aux;
                }).collect(Collectors.toList()));
            }
            return lr;
        }).collect(Collectors.toList()));

        List<AuditMovement> movements = auditLogMapper.selectMovementsByVoucher(entry.getVoucherNo());
        response.setMovements(movements.stream().map(m -> {
            AuditTraceResponse.Movement mr = new AuditTraceResponse.Movement();
            mr.setMovementId(m.getMovementId());
            mr.setDirection(m.getDirection());
            mr.setQuantity(m.getQuantity());
            mr.setItemCode(m.getItemCode());
            mr.setItemName(m.getItemName());
            return mr;
        }).collect(Collectors.toList()));

        log.info("数据追溯: entryId={}, 分录行={}, 辅助核算维度行={}, 流水={}",
                entryId, response.getLines().size(), auxMap.values().stream().mapToInt(List::size).sum(),
                response.getMovements().size());
        return response;
    }
}
