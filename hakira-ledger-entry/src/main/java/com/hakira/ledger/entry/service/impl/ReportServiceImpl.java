package com.hakira.ledger.entry.service.impl;

import com.hakira.ledger.api.dto.report.BalanceSheetResponse;
import com.hakira.ledger.api.dto.report.CashFlowResponse;
import com.hakira.ledger.api.dto.report.IncomeStatementResponse;
import com.hakira.ledger.api.dto.report.LedgerAccountItem;
import com.hakira.ledger.api.dto.report.LedgerEntryItem;
import com.hakira.ledger.api.report.IReportService;
import com.hakira.ledger.entry.mapper.AccountBalanceMapper;
import com.hakira.ledger.entry.mapper.ReportMapper;
import com.hakira.ledger.entry.pojo.AccountBalance;
import com.hakira.ledger.entry.pojo.LedgerRow;
import com.hakira.ledger.entry.pojo.ReportItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 财务报表服务实现（资产负债表 / 利润表 / 现金流量表）
 * <p>
 * 报表读 account_balance 物化值（O(1) 不扫流水），现金流量表按 CASH_FLOW 维度聚合（分区裁剪）。
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements IReportService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String DIRECTION_DEBIT = "D";
    private static final String CF_OPERATING = "CF001";
    private static final String CF_INVESTING = "CF002";
    private static final String CF_FINANCING = "CF003";

    private final ReportMapper reportMapper;
    private final AccountBalanceMapper accountBalanceMapper;

    @Override
    public BalanceSheetResponse getBalanceSheet(String period) {
        List<ReportItem> items = reportMapper.selectBalanceByPeriod(period);
        List<BalanceSheetResponse.Item> assets = new ArrayList<>();
        List<BalanceSheetResponse.Item> liabilities = new ArrayList<>();
        List<BalanceSheetResponse.Item> equity = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (ReportItem item : items) {
            String category = item.getCategory();
            BigDecimal amount;
            if ("资产".equals(category) || "成本".equals(category)) {
                // 资产（含成本）：借正贷负
                amount = nz(item.getClosingDebit()).subtract(nz(item.getClosingCredit()));
                assets.add(buildItem(item, amount));
                totalAssets = totalAssets.add(amount);
            } else if ("负债".equals(category)) {
                // 负债：贷正借负
                amount = nz(item.getClosingCredit()).subtract(nz(item.getClosingDebit()));
                liabilities.add(buildItem(item, amount));
                totalLiabilities = totalLiabilities.add(amount);
            } else if ("权益".equals(category)) {
                // 权益：贷正借负
                amount = nz(item.getClosingCredit()).subtract(nz(item.getClosingDebit()));
                equity.add(buildItem(item, amount));
                totalEquity = totalEquity.add(amount);
            }
        }

        BalanceSheetResponse response = new BalanceSheetResponse();
        response.setPeriod(period);
        response.setAssets(assets);
        response.setLiabilities(liabilities);
        response.setEquity(equity);
        response.setTotalAssets(totalAssets);
        response.setTotalLiabilities(totalLiabilities);
        response.setTotalEquity(totalEquity);
        response.setBalanced(totalAssets.compareTo(totalLiabilities.add(totalEquity)) == 0);
        log.info("资产负债表: 期间={}, 资产={}, 负债={}, 权益={}, 平衡={}",
                period, totalAssets, totalLiabilities, totalEquity, response.isBalanced());
        return response;
    }

    @Override
    public IncomeStatementResponse getIncomeStatement(String period) {
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        List<ReportItem> items = reportMapper.selectProfitLossByPeriod(start, end);
        Map<String, BigDecimal> net = new HashMap<>();
        for (ReportItem item : items) {
            net.merge(item.getSubjectCode(), netAmount(item), BigDecimal::add);
        }

        BigDecimal operatingRevenue = sum(net, "6001", "6051");
        BigDecimal operatingCost = sum(net, "6401", "6402");
        BigDecimal taxSurcharge = sum(net, "6403");
        BigDecimal sellingExpense = sum(net, "6601");
        BigDecimal adminExpense = sum(net, "6602");
        BigDecimal financeExpense = sum(net, "6603");
        BigDecimal investmentIncome = sum(net, "6111");
        BigDecimal nonOperatingIncome = sum(net, "6301");
        BigDecimal nonOperatingExpense = sum(net, "6711");
        BigDecimal incomeTax = sum(net, "6801");

        BigDecimal operatingProfit = operatingRevenue.subtract(operatingCost).subtract(taxSurcharge)
                .subtract(sellingExpense).subtract(adminExpense).subtract(financeExpense).add(investmentIncome);
        BigDecimal totalProfit = operatingProfit.add(nonOperatingIncome).subtract(nonOperatingExpense);
        BigDecimal netProfit = totalProfit.subtract(incomeTax);

        IncomeStatementResponse response = new IncomeStatementResponse();
        response.setPeriod(period);
        response.setOperatingRevenue(operatingRevenue);
        response.setOperatingCost(operatingCost);
        response.setTaxSurcharge(taxSurcharge);
        response.setSellingExpense(sellingExpense);
        response.setAdminExpense(adminExpense);
        response.setFinanceExpense(financeExpense);
        response.setInvestmentIncome(investmentIncome);
        response.setOperatingProfit(operatingProfit);
        response.setNonOperatingIncome(nonOperatingIncome);
        response.setNonOperatingExpense(nonOperatingExpense);
        response.setTotalProfit(totalProfit);
        response.setIncomeTax(incomeTax);
        response.setNetProfit(netProfit);
        log.info("利润表: 期间={}, 营业收入={}, 净利润={}", period, operatingRevenue, netProfit);
        return response;
    }

    @Override
    public CashFlowResponse getCashFlow(String period) {
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        List<ReportItem> items = reportMapper.selectCashFlowByPeriod(start, end);

        Map<String, BigDecimal> inflow = new HashMap<>();
        Map<String, BigDecimal> outflow = new HashMap<>();
        for (ReportItem item : items) {
            inflow.merge(item.getValueCode(), nz(item.getPeriodDebit()), BigDecimal::add);
            outflow.merge(item.getValueCode(), nz(item.getPeriodCredit()), BigDecimal::add);
        }

        BigDecimal operatingInflow = inflow.getOrDefault(CF_OPERATING, BigDecimal.ZERO);
        BigDecimal operatingOutflow = outflow.getOrDefault(CF_OPERATING, BigDecimal.ZERO);
        BigDecimal investingInflow = inflow.getOrDefault(CF_INVESTING, BigDecimal.ZERO);
        BigDecimal investingOutflow = outflow.getOrDefault(CF_INVESTING, BigDecimal.ZERO);
        BigDecimal financingInflow = inflow.getOrDefault(CF_FINANCING, BigDecimal.ZERO);
        BigDecimal financingOutflow = outflow.getOrDefault(CF_FINANCING, BigDecimal.ZERO);

        BigDecimal operatingNet = operatingInflow.subtract(operatingOutflow);
        BigDecimal investingNet = investingInflow.subtract(investingOutflow);
        BigDecimal financingNet = financingInflow.subtract(financingOutflow);

        CashFlowResponse response = new CashFlowResponse();
        response.setPeriod(period);
        response.setOperatingInflow(operatingInflow);
        response.setOperatingOutflow(operatingOutflow);
        response.setOperatingNet(operatingNet);
        response.setInvestingInflow(investingInflow);
        response.setInvestingOutflow(investingOutflow);
        response.setInvestingNet(investingNet);
        response.setFinancingInflow(financingInflow);
        response.setFinancingOutflow(financingOutflow);
        response.setFinancingNet(financingNet);
        response.setNetIncrease(operatingNet.add(investingNet).add(financingNet));
        log.info("现金流量表: 期间={}, 经营净流量={}, 投资净流量={}, 筹资净流量={}",
                period, operatingNet, investingNet, financingNet);
        return response;
    }

    @Override
    public List<LedgerAccountItem> getLedger(String period) {
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        List<AccountBalance> balances = accountBalanceMapper.selectByPeriod(period);
        Map<String, List<LedgerEntryItem>> detailsBySubject = reportMapper.selectLedgerDetails(start, end)
                .stream()
                .collect(Collectors.groupingBy(LedgerRow::getSubjectCode,
                        Collectors.mapping(this::toEntryItem, Collectors.toList())));
        List<LedgerAccountItem> result = new ArrayList<>();
        for (AccountBalance b : balances) {
            LedgerAccountItem item = new LedgerAccountItem();
            item.setSubjectCode(b.getSubjectCode());
            item.setSubjectName(b.getSubjectName());
            item.setOpeningDebit(nz(b.getOpeningDebit()));
            item.setOpeningCredit(nz(b.getOpeningCredit()));
            item.setPeriodDebit(nz(b.getPeriodDebit()));
            item.setPeriodCredit(nz(b.getPeriodCredit()));
            item.setClosingDebit(nz(b.getClosingDebit()));
            item.setClosingCredit(nz(b.getClosingCredit()));
            item.setEntries(detailsBySubject.get(b.getSubjectCode()));
            result.add(item);
        }
        log.info("总账: 期间={}, 科目数={}", period, result.size());
        return result;
    }

    @Override
    public List<LedgerEntryItem> getDetailLedger(String subjectCode, String period) {
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        return reportMapper.selectDetailLedger(subjectCode, start, end).stream()
                .map(this::toEntryItem).collect(Collectors.toList());
    }

    @Override
    public List<LedgerEntryItem> getJournal(String period) {
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        return reportMapper.selectJournal(start, end).stream()
                .map(this::toEntryItem).collect(Collectors.toList());
    }

    private LedgerEntryItem toEntryItem(LedgerRow row) {
        LedgerEntryItem item = new LedgerEntryItem();
        item.setVoucherNo(row.getVoucherNo());
        item.setEntryDate(row.getEntryDate() != null ? row.getEntryDate().toString() : null);
        item.setDescription(row.getDescription());
        item.setDebitAmount(nz(row.getDebitAmount()));
        item.setCreditAmount(nz(row.getCreditAmount()));
        return item;
    }

    /** 损益科目净额：D 方向 = 借 - 贷（费用正），C 方向 = 贷 - 借（收入正） */
    private BigDecimal netAmount(ReportItem item) {
        if (DIRECTION_DEBIT.equals(item.getBalanceDirection())) {
            return nz(item.getPeriodDebit()).subtract(nz(item.getPeriodCredit()));
        }
        return nz(item.getPeriodCredit()).subtract(nz(item.getPeriodDebit()));
    }

    private BalanceSheetResponse.Item buildItem(ReportItem item, BigDecimal amount) {
        BalanceSheetResponse.Item r = new BalanceSheetResponse.Item();
        r.setSubjectCode(item.getSubjectCode());
        r.setSubjectName(item.getSubjectName());
        r.setAmount(amount);
        return r;
    }

    private BigDecimal sum(Map<String, BigDecimal> map, String... codes) {
        BigDecimal result = BigDecimal.ZERO;
        for (String code : codes) {
            result = result.add(map.getOrDefault(code, BigDecimal.ZERO));
        }
        return result;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
