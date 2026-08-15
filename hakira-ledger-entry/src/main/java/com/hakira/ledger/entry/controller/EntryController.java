package com.hakira.ledger.entry.controller;

import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.api.dto.entry.JournalEntryRequest;
import com.hakira.ledger.api.dto.entry.JournalEntryResponse;
import com.hakira.ledger.api.dto.entry.EntrySearchRequest;
import com.hakira.ledger.api.entry.IEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entry")
@Slf4j
@RequiredArgsConstructor
public class EntryController {
    private final IEntryService entryService;

    @PostMapping("/post")
    public Result<JournalEntryResponse> postEntry(@RequestBody JournalEntryRequest request) {
        log.info("收到记账请求: voucherNo={}", request.getVoucherNo());
        JournalEntryResponse response = entryService.postEntry(request);
        return Result.returnSuccess(response);
    }

    @GetMapping("/{entryId}")
    public Result<JournalEntryResponse> getEntry(@PathVariable("entryId") String entryId) {
        return Result.returnSuccess(entryService.getEntry(entryId));
    }

    @PostMapping("/search")
    public Result<List<JournalEntryResponse>> searchEntries(@RequestBody EntrySearchRequest request) {
        return Result.returnSuccess(entryService.searchEntries(request));
    }

    @PostMapping("/reverse/{entryId}")
    public Result<JournalEntryResponse> reverseEntry(@PathVariable("entryId") String entryId) {
        log.info("收到冲销请求: entryId={}", entryId);
        return Result.returnSuccess(entryService.reverseEntry(entryId));
    }

    @PostMapping("/void/{entryId}")
    public Result<JournalEntryResponse> voidEntry(@PathVariable("entryId") String entryId) {
        return Result.returnSuccess(entryService.voidEntry(entryId));
    }

    @PostMapping("/submit/{entryId}")
    public Result<JournalEntryResponse> submitForApproval(@PathVariable("entryId") String entryId) {
        return Result.returnSuccess(entryService.submitForApproval(entryId));
    }

    @PostMapping("/approve/{entryId}")
    public Result<JournalEntryResponse> approveEntry(@PathVariable("entryId") String entryId) {
        return Result.returnSuccess(entryService.approveEntry(entryId));
    }

    @PostMapping("/reject/{entryId}")
    public Result<JournalEntryResponse> rejectEntry(@PathVariable("entryId") String entryId) {
        return Result.returnSuccess(entryService.rejectEntry(entryId));
    }
}
