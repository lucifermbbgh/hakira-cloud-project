package com.hakira.ledger.api.dto.cost;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 制造费用分配结果
 */
@Data
public class CostAllocateResponse {
    private String period;
    /** 分配金额 */
    private BigDecimal allocatedAmount;
    /** 分配凭证号 */
    private String voucherNo;
}
