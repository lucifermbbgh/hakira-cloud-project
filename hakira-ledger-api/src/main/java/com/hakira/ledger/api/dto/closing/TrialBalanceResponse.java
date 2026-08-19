package com.hakira.ledger.api.dto.closing;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 试算平衡表结果
 */
@Data
public class TrialBalanceResponse {
    /** 是否平衡 */
    private boolean balanced;
    /** 借方发生额合计 */
    private BigDecimal totalDebit;
    /** 贷方发生额合计 */
    private BigDecimal totalCredit;
    /** 差额（借-贷） */
    private BigDecimal difference;
    /** 科目明细 */
    private List<AccountBalanceResponse> details;
}
