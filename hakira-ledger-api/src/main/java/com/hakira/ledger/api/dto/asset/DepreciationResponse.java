package com.hakira.ledger.api.dto.asset;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 折旧计提结果
 */
@Data
public class DepreciationResponse {
    private String period;
    /** 计提资产数 */
    private int depreciatedCount;
    /** 总折旧额 */
    private BigDecimal totalDepreciation;
    /** 折旧凭证号列表 */
    private List<String> voucherNos;
}
