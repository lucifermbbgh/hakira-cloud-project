package com.hakira.ledger.api.dto.closing;

import lombok.Data;

import java.util.List;

/**
 * 结账 / 损益结转结果
 */
@Data
public class ClosingResponse {
    /** 会计期间 YYYYMM */
    private String period;
    /** 期间状态 */
    private String status;
    /** 生成的结转凭证号列表 */
    private List<String> voucherNos;
    /** 结转科目数 */
    private int transferCount;
    /** 物化科目数（月结时） */
    private int balanceCount;
}
