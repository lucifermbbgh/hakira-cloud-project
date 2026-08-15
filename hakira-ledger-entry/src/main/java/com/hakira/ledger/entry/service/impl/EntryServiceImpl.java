package com.hakira.ledger.entry.service.impl;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.audit.IAuditService;
import com.hakira.ledger.api.dto.entry.EntrySearchRequest;
import com.hakira.ledger.api.dto.entry.JournalEntryRequest;
import com.hakira.ledger.api.dto.entry.JournalEntryResponse;
import com.hakira.ledger.api.entry.IEntryService;
import com.hakira.ledger.entry.mapper.AccountingPeriodMapper;
import com.hakira.ledger.entry.mapper.AccountSubjectMapper;
import com.hakira.ledger.entry.mapper.AuxiliaryMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineAuxMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineMapper;
import com.hakira.ledger.entry.mapper.JournalEntryMapper;
import com.hakira.ledger.entry.pojo.AccountingPeriod;
import com.hakira.ledger.entry.pojo.JournalEntry;
import com.hakira.ledger.entry.pojo.JournalEntryLine;
import com.hakira.ledger.entry.pojo.JournalEntryLineAux;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分录服务实现（MySQL 持久化，分录头 + 分录行 事务写入）
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EntryServiceImpl implements IEntryService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter VOUCHER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_POSTED = "POSTED";
    private static final String STATUS_REVERSED = "REVERSED";
    private static final String STATUS_VOID = "VOID";
    private static final String STATUS_CLOSED = "CLOSED";

    private final JournalEntryMapper journalEntryMapper;
    private final JournalEntryLineMapper journalEntryLineMapper;
    private final AccountSubjectMapper accountSubjectMapper;
    private final JournalEntryLineAuxMapper journalEntryLineAuxMapper;
    private final AuxiliaryMapper auxiliaryMapper;
    private final AccountingPeriodMapper accountingPeriodMapper;
    private final IAuditService auditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JournalEntryResponse postEntry(JournalEntryRequest request) {
        // 1. 借贷平衡校验
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (JournalEntryRequest.EntryLine e : request.getEntries()) {
            if (e.getDebitAmount() != null) {
                totalDebit = totalDebit.add(e.getDebitAmount());
            }
            if (e.getCreditAmount() != null) {
                totalCredit = totalCredit.add(e.getCreditAmount());
            }
        }
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BizException(BizErrorCode.ENTRY_UNBALANCED,
                    String.format("借方=%s, 贷方=%s", totalDebit, totalCredit));
        }

        // 2. 校验会计科目合法性（科目必须在国家标准科目表存在）
        for (JournalEntryRequest.EntryLine e : request.getEntries()) {
            if (accountSubjectMapper.countActiveByCode(e.getAccountCode()) == 0) {
                throw new BizException(BizErrorCode.ACCOUNT_NOT_FOUND, e.getAccountCode());
            }
        }

        // 3. 生成分录ID + 解析记账日期
        String entryId = IdGeneratorUtil.getId();
        LocalDate entryDate = parseDate(request.getEntryDate());

        // 3.1 校验会计期间可录入（CLOSED 期间拒绝，Phase 9）
        checkPeriodWritable(entryDate);

        // 4. 写入分录头（凭证号：未传则自动生成）
        JournalEntry entry = new JournalEntry();
        entry.setEntryId(entryId);
        entry.setEntryDate(entryDate);
        entry.setVoucherNo(generateVoucherNo(request.getVoucherNo(), entryDate));
        entry.setDescription(request.getDescription());
        entry.setTotalDebit(totalDebit);
        entry.setTotalCredit(totalCredit);
        entry.setStatus(Boolean.TRUE.equals(request.getDraft()) ? STATUS_DRAFT : STATUS_POSTED);
        journalEntryMapper.insert(entry);

        // 5. 写入分录行
        List<JournalEntryLine> lines = new ArrayList<>();
        int lineNo = 1;
        for (JournalEntryRequest.EntryLine e : request.getEntries()) {
            JournalEntryLine line = new JournalEntryLine();
            line.setEntryId(entryId);
            line.setEntryDate(entryDate);
            line.setLineNo(lineNo++);
            line.setSubjectCode(e.getAccountCode());
            line.setSubjectName(e.getAccountName());
            line.setDescription(e.getDescription());
            line.setDebitAmount(e.getDebitAmount());
            line.setCreditAmount(e.getCreditAmount());
            journalEntryLineMapper.insert(line);
            lines.add(line);
            // 写入辅助核算维度关联（校验维度与值合法性）
            writeAux(line.getLineId(), e.getAux());
        }

        log.info("记账成功: entryId={}, voucherNo={}, 借方={}, 贷方={}",
                entryId, request.getVoucherNo(), totalDebit, totalCredit);
        auditService.record("POST_ENTRY", "JOURNAL_ENTRY", entryId,
                "凭证号=" + entry.getVoucherNo() + ", 借方=" + totalDebit + ", 贷方=" + totalCredit);
        return buildResponse(entry, lines);
    }

    @Override
    public JournalEntryResponse getEntry(String entryId) {
        JournalEntry entry = journalEntryMapper.selectByEntryId(entryId);
        if (entry == null) {
            throw new BizException(BizErrorCode.ENTRY_NOT_FOUND, entryId);
        }
        List<JournalEntryLine> lines = journalEntryLineMapper.selectByEntryId(entryId, entry.getEntryDate());
        return buildResponse(entry, lines);
    }

    @Override
    public List<JournalEntryResponse> searchEntries(EntrySearchRequest request) {
        List<JournalEntry> entries = journalEntryMapper.search(
                request.getFromDate(), request.getToDate(),
                request.getAccountCode(), request.getStatus());
        return entries.stream()
                .map(e -> buildResponse(e, journalEntryLineMapper.selectByEntryId(e.getEntryId(), e.getEntryDate())))
                .collect(Collectors.toList());
    }

    /** 头 + 行 -> 响应对象 */
    private JournalEntryResponse buildResponse(JournalEntry entry, List<JournalEntryLine> lines) {
        JournalEntryResponse response = new JournalEntryResponse();
        response.setEntryId(entry.getEntryId());
        response.setVoucherNo(entry.getVoucherNo());
        response.setEntryDate(entry.getEntryDate() != null ? entry.getEntryDate().format(DATE_FORMATTER) : null);
        response.setDescription(entry.getDescription());
        response.setTotalDebit(entry.getTotalDebit());
        response.setTotalCredit(entry.getTotalCredit());
        response.setStatus(entry.getStatus());

        // 查询辅助核算维度，按 lineId 分组（Phase 7）
        Map<Long, List<JournalEntryResponse.AuxItem>> auxMap = journalEntryLineAuxMapper
                .selectByEntryId(entry.getEntryId(), entry.getEntryDate())
                .stream()
                .collect(Collectors.groupingBy(
                        JournalEntryLineAux::getLineId,
                        Collectors.mapping(a -> {
                            JournalEntryResponse.AuxItem item = new JournalEntryResponse.AuxItem();
                            item.setDimensionCode(a.getDimensionCode());
                            item.setValueCode(a.getValueCode());
                            return item;
                        }, Collectors.toList())
                ));

        List<JournalEntryResponse.EntryLineResponse> lineResponses = lines.stream().map(l -> {
            JournalEntryResponse.EntryLineResponse lr = new JournalEntryResponse.EntryLineResponse();
            lr.setAccountCode(l.getSubjectCode());
            lr.setAccountName(l.getSubjectName());
            lr.setDescription(l.getDescription());
            lr.setDebitAmount(l.getDebitAmount());
            lr.setCreditAmount(l.getCreditAmount());
            lr.setAux(auxMap.get(l.getLineId()));
            return lr;
        }).collect(Collectors.toList());
        response.setEntries(lineResponses);
        return response;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /** 校验会计期间可录入（CLOSED 期间拒绝；首次录入自动建 OPEN 期间，Phase 9 期间锁定） */
    private void checkPeriodWritable(LocalDate entryDate) {
        String period = entryDate.format(PERIOD_FORMATTER);
        AccountingPeriod ap = accountingPeriodMapper.selectByPeriod(period);
        if (ap != null && STATUS_CLOSED.equals(ap.getStatus())) {
            throw new BizException(BizErrorCode.PERIOD_CLOSED, period);
        }
        if (ap == null) {
            accountingPeriodMapper.insertOpen(period);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JournalEntryResponse reverseEntry(String entryId) {
        // 1. 查询原分录
        JournalEntry entry = journalEntryMapper.selectByEntryId(entryId);
        if (entry == null) {
            throw new BizException(BizErrorCode.ENTRY_NOT_FOUND, entryId);
        }
        // 2. 校验状态：仅 POSTED 可冲销
        if (!STATUS_POSTED.equals(entry.getStatus())) {
            throw new BizException(BizErrorCode.ENTRY_STATUS_INVALID,
                    String.format("凭证 %s 状态为 %s，不可冲销", entryId, entry.getStatus()));
        }
        // 3. 查询原分录行
        List<JournalEntryLine> lines = journalEntryLineMapper.selectByEntryId(entryId, entry.getEntryDate());
        // 4. 查询原辅助核算，按 lineId 分组
        Map<Long, List<JournalEntryLineAux>> auxMap = journalEntryLineAuxMapper
                .selectByEntryId(entryId, entry.getEntryDate())
                .stream()
                .collect(Collectors.groupingBy(JournalEntryLineAux::getLineId));

        // 5. 生成反向分录头
        String reverseId = IdGeneratorUtil.getId();
        LocalDate reverseDate = LocalDate.now();
        JournalEntry reverse = new JournalEntry();
        reverse.setEntryId(reverseId);
        reverse.setEntryDate(reverseDate);
        reverse.setVoucherNo(generateVoucherNo(null, reverseDate));
        reverse.setDescription("冲销原凭证 " + entry.getVoucherNo());
        reverse.setTotalDebit(entry.getTotalCredit());
        reverse.setTotalCredit(entry.getTotalDebit());
        reverse.setStatus(STATUS_POSTED);
        journalEntryMapper.insert(reverse);

        // 6. 写反向分录行（借贷互换 + 复制辅助核算）
        List<JournalEntryLine> reverseLines = new ArrayList<>();
        int lineNo = 1;
        for (JournalEntryLine l : lines) {
            JournalEntryLine rl = new JournalEntryLine();
            rl.setEntryId(reverseId);
            rl.setEntryDate(reverseDate);
            rl.setLineNo(lineNo++);
            rl.setSubjectCode(l.getSubjectCode());
            rl.setSubjectName(l.getSubjectName());
            rl.setDescription(l.getDescription());
            rl.setDebitAmount(l.getCreditAmount());
            rl.setCreditAmount(l.getDebitAmount());
            journalEntryLineMapper.insert(rl);
            reverseLines.add(rl);
            List<JournalEntryLineAux> originAux = auxMap.get(l.getLineId());
            if (originAux != null) {
                for (JournalEntryLineAux a : originAux) {
                    journalEntryLineAuxMapper.insert(rl.getLineId(), a.getDimensionCode(), a.getValueCode());
                }
            }
        }

        // 7. 原分录置 REVERSED（乐观锁）
        int updated = journalEntryMapper.updateStatus(entryId, STATUS_REVERSED, entry.getVersion());
        if (updated == 0) {
            throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, entryId);
        }

        log.info("冲销成功: 原凭证={}, 冲销凭证={}", entryId, reverseId);
        auditService.record("REVERSE_ENTRY", "JOURNAL_ENTRY", entryId,
                "冲销凭证=" + reverse.getVoucherNo());
        return buildResponse(reverse, reverseLines);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JournalEntryResponse voidEntry(String entryId) {
        changeStatus(entryId, Set.of(STATUS_POSTED, STATUS_DRAFT), STATUS_VOID);
        return getEntry(entryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JournalEntryResponse submitForApproval(String entryId) {
        changeStatus(entryId, Set.of(STATUS_DRAFT), STATUS_PENDING);
        return getEntry(entryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JournalEntryResponse approveEntry(String entryId) {
        changeStatus(entryId, Set.of(STATUS_PENDING), STATUS_POSTED);
        return getEntry(entryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JournalEntryResponse rejectEntry(String entryId) {
        changeStatus(entryId, Set.of(STATUS_PENDING), STATUS_DRAFT);
        return getEntry(entryId);
    }

    /** 通用状态流转（乐观锁）：校验当前状态 → 更新目标状态 */
    private void changeStatus(String entryId, Set<String> expectedStatuses, String targetStatus) {
        JournalEntry entry = journalEntryMapper.selectByEntryId(entryId);
        if (entry == null) {
            throw new BizException(BizErrorCode.ENTRY_NOT_FOUND, entryId);
        }
        if (!expectedStatuses.contains(entry.getStatus())) {
            throw new BizException(BizErrorCode.ENTRY_STATUS_INVALID,
                    String.format("凭证 %s 状态为 %s，不允许此操作", entryId, entry.getStatus()));
        }
        int updated = journalEntryMapper.updateStatus(entryId, targetStatus, entry.getVersion());
        if (updated == 0) {
            throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, entryId);
        }
    }

    /** 凭证号：未传则自动生成 PZ-YYYYMMDD-NNN */
    private String generateVoucherNo(String voucherNo, LocalDate entryDate) {
        if (voucherNo != null && !voucherNo.isEmpty()) {
            return voucherNo;
        }
        String dateStr = entryDate.format(VOUCHER_DATE_FORMATTER);
        String maxVoucherNo = journalEntryMapper.selectMaxVoucherNo(dateStr);
        int seq = 1;
        if (maxVoucherNo != null && !maxVoucherNo.isEmpty()) {
            int idx = maxVoucherNo.lastIndexOf('-');
            if (idx >= 0) {
                try {
                    seq = Integer.parseInt(maxVoucherNo.substring(idx + 1)) + 1;
                } catch (NumberFormatException ignored) {
                    // 非数字后缀，从 1 开始
                }
            }
        }
        return String.format("PZ-%s-%03d", dateStr, seq);
    }

    /** 写入分录行辅助核算维度（校验维度与值合法性，Phase 7） */
    private void writeAux(Long lineId, List<JournalEntryRequest.AuxEntry> auxList) {
        if (auxList == null || auxList.isEmpty()) {
            return;
        }
        for (JournalEntryRequest.AuxEntry aux : auxList) {
            if (auxiliaryMapper.countActiveDimension(aux.getDimensionCode()) == 0) {
                throw new BizException(BizErrorCode.AUX_DIMENSION_NOT_FOUND, aux.getDimensionCode());
            }
            if (auxiliaryMapper.countActiveValue(aux.getDimensionCode(), aux.getValueCode()) == 0) {
                throw new BizException(BizErrorCode.AUX_VALUE_NOT_FOUND,
                        aux.getDimensionCode() + "/" + aux.getValueCode());
            }
            journalEntryLineAuxMapper.insert(lineId, aux.getDimensionCode(), aux.getValueCode());
        }
    }
}
