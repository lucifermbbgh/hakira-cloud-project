package com.hakira.ledger.api.dto.budget;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预算编制请求
 */
@Data
public class BudgetSetRequest {
    private String period;
    private String subjectCode;
    private BigDecimal budgetAmount;
}
