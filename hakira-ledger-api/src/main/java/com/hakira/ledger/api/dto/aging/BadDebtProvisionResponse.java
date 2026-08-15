package com.hakira.ledger.api.dto.aging;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 坏账准备计提结果
 */
@Data
public class BadDebtProvisionResponse {
    private String asOfDate;
    /** 计提/冲回金额（正=补提，负=冲回） */
    private BigDecimal provisionAmount;
    /** 计提凭证号 */
    private String voucherNo;
    /** 各账龄段应计提额 */
    private List<AgeBucket> buckets;

    @Data
    public static class AgeBucket {
        private String bucket;
        private BigDecimal balance;
        private BigDecimal rate;
        private BigDecimal provision;
    }
}
