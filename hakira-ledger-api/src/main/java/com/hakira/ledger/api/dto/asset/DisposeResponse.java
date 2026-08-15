package com.hakira.ledger.api.dto.asset;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资产处置结果
 */
@Data
public class DisposeResponse {
    private String assetCode;
    /** 处置凭证号 */
    private String voucherNo;
    /** 处置时净值 */
    private BigDecimal netValue;
}
