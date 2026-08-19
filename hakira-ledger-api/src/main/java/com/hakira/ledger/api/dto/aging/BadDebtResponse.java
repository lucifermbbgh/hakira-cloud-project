package com.hakira.ledger.api.dto.aging;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 坏账核销/收回结果
 */
@Data
public class BadDebtResponse {
    private List<String> voucherNos;
    private BigDecimal amount;
}
