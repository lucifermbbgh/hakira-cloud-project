package com.hakira.ledger.entry.service.impl;

import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.dto.entry.JournalEntryRequest;
import com.hakira.ledger.api.dto.entry.JournalEntryResponse;
import com.hakira.ledger.api.dto.entry.EntrySearchRequest;
import com.hakira.ledger.api.entry.IEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EntryServiceImpl implements IEntryService {

    private final ConcurrentHashMap<String, JournalEntryResponse> store = new ConcurrentHashMap<>();

    @Override
    public JournalEntryResponse postEntry(JournalEntryRequest request) {
        // 1. 校验借贷平衡
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
            throw new IllegalArgumentException(
                    String.format("借贷不平衡: 借方=%s, 贷方=%s", totalDebit, totalCredit));
        }

        // 2. 生成分录ID
        String entryId = IdGeneratorUtil.getId();

        // 3. 构建响应
        JournalEntryResponse response = new JournalEntryResponse();
        response.setEntryId(entryId);
        response.setVoucherNo(request.getVoucherNo());
        response.setEntryDate(request.getEntryDate());
        response.setDescription(request.getDescription());
        response.setTotalDebit(totalDebit);
        response.setTotalCredit(totalCredit);
        response.setStatus("POSTED");

        List<JournalEntryResponse.EntryLineResponse> lineResponses = new ArrayList<>();
        for (JournalEntryRequest.EntryLine e : request.getEntries()) {
            JournalEntryResponse.EntryLineResponse lineResp = new JournalEntryResponse.EntryLineResponse();
            lineResp.setAccountCode(e.getAccountCode());
            lineResp.setAccountName(e.getAccountName());
            lineResp.setDescription(e.getDescription());
            lineResp.setDebitAmount(e.getDebitAmount());
            lineResp.setCreditAmount(e.getCreditAmount());
            lineResponses.add(lineResp);
        }
        response.setEntries(lineResponses);

        store.put(entryId, response);

        log.info("记账成功: entryId={}, voucherNo={}, 借方={}, 贷方={}",
                entryId, request.getVoucherNo(), totalDebit, totalCredit);
        return response;
    }

    @Override
    public JournalEntryResponse getEntry(String entryId) {
        JournalEntryResponse response = store.get(entryId);
        if (response == null) {
            throw new IllegalArgumentException("分录不存在: " + entryId);
        }
        return response;
    }

    @Override
    public List<JournalEntryResponse> searchEntries(EntrySearchRequest request) {
        return store.values().stream()
                .filter(e -> {
                    // 按日期范围筛选
                    if (request.getFromDate() != null && e.getEntryDate() != null
                            && e.getEntryDate().compareTo(request.getFromDate()) < 0) {
                        return false;
                    }
                    if (request.getToDate() != null && e.getEntryDate() != null
                            && e.getEntryDate().compareTo(request.getToDate()) > 0) {
                        return false;
                    }
                    // 按科目编码筛选
                    if (request.getAccountCode() != null && !request.getAccountCode().isEmpty()) {
                        boolean hasAccount = false;
                        if (e.getEntries() != null) {
                            for (JournalEntryResponse.EntryLineResponse line : e.getEntries()) {
                                if (request.getAccountCode().equals(line.getAccountCode())) {
                                    hasAccount = true;
                                    break;
                                }
                            }
                        }
                        if (!hasAccount) {
                            return false;
                        }
                    }
                    // 按状态筛选
                    if (request.getStatus() != null && !request.getStatus().isEmpty()
                            && !request.getStatus().equals(e.getStatus())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}
