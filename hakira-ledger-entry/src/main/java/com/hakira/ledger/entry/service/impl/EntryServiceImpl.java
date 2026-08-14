package com.hakira.ledger.entry.service.impl;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.dto.entry.EntrySearchRequest;
import com.hakira.ledger.api.dto.entry.JournalEntryRequest;
import com.hakira.ledger.api.dto.entry.JournalEntryResponse;
import com.hakira.ledger.api.entry.IEntryService;
import com.hakira.ledger.entry.mapper.AccountSubjectMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineMapper;
import com.hakira.ledger.entry.mapper.JournalEntryMapper;
import com.hakira.ledger.entry.pojo.JournalEntry;
import com.hakira.ledger.entry.pojo.JournalEntryLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    private final JournalEntryMapper journalEntryMapper;
    private final JournalEntryLineMapper journalEntryLineMapper;
    private final AccountSubjectMapper accountSubjectMapper;

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

        // 4. 写入分录头
        JournalEntry entry = new JournalEntry();
        entry.setEntryId(entryId);
        entry.setEntryDate(entryDate);
        entry.setVoucherNo(request.getVoucherNo());
        entry.setDescription(request.getDescription());
        entry.setTotalDebit(totalDebit);
        entry.setTotalCredit(totalCredit);
        entry.setStatus("POSTED");
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
        }

        log.info("记账成功: entryId={}, voucherNo={}, 借方={}, 贷方={}",
                entryId, request.getVoucherNo(), totalDebit, totalCredit);
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

        List<JournalEntryResponse.EntryLineResponse> lineResponses = lines.stream().map(l -> {
            JournalEntryResponse.EntryLineResponse lr = new JournalEntryResponse.EntryLineResponse();
            lr.setAccountCode(l.getSubjectCode());
            lr.setAccountName(l.getSubjectName());
            lr.setDescription(l.getDescription());
            lr.setDebitAmount(l.getDebitAmount());
            lr.setCreditAmount(l.getCreditAmount());
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
}
