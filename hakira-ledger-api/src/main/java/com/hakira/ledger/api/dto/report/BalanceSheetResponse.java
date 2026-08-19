package com.hakira.ledger.api.dto.report;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 资产负债表响应（资产 = 负债 + 所有者权益）
 */
@Data
public class BalanceSheetResponse {
    private String period;
    /** 资产（含成本类并入存货） */
    private List<Item> assets;
    /** 负债 */
    private List<Item> liabilities;
    /** 所有者权益 */
    private List<Item> equity;
    /** 资产合计 */
    private BigDecimal totalAssets;
    /** 负债合计 */
    private BigDecimal totalLiabilities;
    /** 权益合计 */
    private BigDecimal totalEquity;
    /** 是否平衡（资产 = 负债 + 权益） */
    private boolean balanced;

    @Data
    public static class Item {
        private String subjectCode;
        private String subjectName;
        /** 期末余额（归约后） */
        private BigDecimal amount;
    }
}
