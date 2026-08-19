package com.hakira.ledger.api.dto.asset;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 固定资产登记请求
 */
@Data
public class FixedAssetRequest {
    private String assetCode;
    private String assetName;
    private String category;
    /** 原值 */
    private BigDecimal originalValue;
    /** 残值率（默认 0.05） */
    private BigDecimal residualRate;
    /** 折旧年限（月） */
    private Integer usefulLife;
    /** 折旧方法：STRAIGHT_LINE / DOUBLE_DECLINING */
    private String depreciationMethod;
    /** 购置日期 */
    private LocalDate purchaseDate;
}
