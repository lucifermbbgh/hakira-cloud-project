package com.hakira.ledger.api.dto.report;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 利润表响应（收入 - 费用 = 净利润）
 */
@Data
public class IncomeStatementResponse {
    private String period;
    /** 营业收入 */
    private BigDecimal operatingRevenue;
    /** 营业成本 */
    private BigDecimal operatingCost;
    /** 税金及附加 */
    private BigDecimal taxSurcharge;
    /** 销售费用 */
    private BigDecimal sellingExpense;
    /** 管理费用 */
    private BigDecimal adminExpense;
    /** 财务费用 */
    private BigDecimal financeExpense;
    /** 投资收益 */
    private BigDecimal investmentIncome;
    /** 营业利润 */
    private BigDecimal operatingProfit;
    /** 营业外收入 */
    private BigDecimal nonOperatingIncome;
    /** 营业外支出 */
    private BigDecimal nonOperatingExpense;
    /** 利润总额 */
    private BigDecimal totalProfit;
    /** 所得税费用 */
    private BigDecimal incomeTax;
    /** 净利润 */
    private BigDecimal netProfit;
}
