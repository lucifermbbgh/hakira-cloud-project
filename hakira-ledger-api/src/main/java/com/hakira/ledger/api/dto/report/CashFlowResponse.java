package com.hakira.ledger.api.dto.report;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 现金流量表响应（直接法，按 CASH_FLOW 维度聚合现金科目）
 */
@Data
public class CashFlowResponse {
    private String period;
    /** 经营活动流入 */
    private BigDecimal operatingInflow;
    /** 经营活动流出 */
    private BigDecimal operatingOutflow;
    /** 经营活动净流量 */
    private BigDecimal operatingNet;
    /** 投资活动流入 */
    private BigDecimal investingInflow;
    /** 投资活动流出 */
    private BigDecimal investingOutflow;
    /** 投资活动净流量 */
    private BigDecimal investingNet;
    /** 筹资活动流入 */
    private BigDecimal financingInflow;
    /** 筹资活动流出 */
    private BigDecimal financingOutflow;
    /** 筹资活动净流量 */
    private BigDecimal financingNet;
    /** 现金及现金等价物净增加额 */
    private BigDecimal netIncrease;
}
