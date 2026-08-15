package com.hakira.ledger.api.entry;

import com.hakira.ledger.api.dto.entry.JournalEntryRequest;
import com.hakira.ledger.api.dto.entry.JournalEntryResponse;
import com.hakira.ledger.api.dto.entry.EntrySearchRequest;

import java.util.List;

/**
 * 会计分录服务接口 — 复式记账核心
 */
public interface IEntryService {
    /** 录入一笔记账凭证（借贷分录） */
    JournalEntryResponse postEntry(JournalEntryRequest request);

    /** 查询分录详情 */
    JournalEntryResponse getEntry(String entryId);

    /** 按条件查询分录列表 */
    List<JournalEntryResponse> searchEntries(EntrySearchRequest request);

    /** 冲销凭证（生成反向分录，原凭证置 REVERSED） */
    JournalEntryResponse reverseEntry(String entryId);
}
