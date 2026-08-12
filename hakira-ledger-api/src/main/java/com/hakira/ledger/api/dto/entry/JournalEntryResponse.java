package com.hakira.ledger.api.dto.entry;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class JournalEntryResponse {
    private String entryId;
    private String voucherNo;
    private String entryDate;
    private String description;
    private BigDecimal totalDebit;     // 借方合计
    private BigDecimal totalCredit;    // 贷方合计
    private String status;              // DRAFT/POSTED/CANCELLED
    private List<EntryLineResponse> entries;

    @Data
    public static class EntryLineResponse {
        private String accountCode;
        private String accountName;
        private String description;
        private BigDecimal debitAmount;
        private BigDecimal creditAmount;
    }
}
