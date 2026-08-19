package com.hakira.ledger.api.dto.entry;

import lombok.Data;

@Data
public class EntrySearchRequest {
    private String fromDate;
    private String toDate;
    private String accountCode;
    private String status;
}
