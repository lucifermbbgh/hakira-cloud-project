package com.hakira.ledger.api.dto.asset;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 固定资产卡片响应
 */
@Data
public class FixedAssetResponse {
    private String assetCode;
    private String assetName;
    private String category;
    private BigDecimal originalValue;
    private BigDecimal residualRate;
    private Integer usefulLife;
    private String depreciationMethod;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netValue;
    private String status;
    private LocalDate purchaseDate;
}
