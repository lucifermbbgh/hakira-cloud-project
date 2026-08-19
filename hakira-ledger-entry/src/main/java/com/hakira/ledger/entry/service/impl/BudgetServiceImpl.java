package com.hakira.ledger.entry.service.impl;

import com.hakira.ledger.api.budget.IBudgetService;
import com.hakira.ledger.api.dto.budget.BudgetItem;
import com.hakira.ledger.api.dto.budget.BudgetResponse;
import com.hakira.ledger.api.dto.budget.BudgetSetRequest;
import com.hakira.ledger.entry.mapper.BudgetMapper;
import com.hakira.ledger.entry.pojo.Budget;
import com.hakira.ledger.entry.pojo.ReportItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预算管理服务实现（预算编制 / 执行监控 / 差异分析）
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BudgetServiceImpl implements IBudgetService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final String DIRECTION_DEBIT = "D";

    private final BudgetMapper budgetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setBudget(BudgetSetRequest request) {
        Budget budget = new Budget();
        budget.setPeriod(request.getPeriod());
        budget.setSubjectCode(request.getSubjectCode());
        budget.setBudgetAmount(request.getBudgetAmount());
        budgetMapper.upsert(budget);
        log.info("编制预算: 期间={}, 科目={}, 金额={}",
                request.getPeriod(), request.getSubjectCode(), request.getBudgetAmount());
    }

    @Override
    public BudgetResponse query(String period) {
        return buildResponse(period, false);
    }

    @Override
    public BudgetResponse variance(String period) {
        return buildResponse(period, true);
    }

    private BudgetResponse buildResponse(String period, boolean onlyOverBudget) {
        List<Budget> budgets = budgetMapper.selectByPeriod(period);
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        Map<String, ReportItem> actualMap = new HashMap<>();
        for (ReportItem item : budgetMapper.selectActualByPeriod(start, end)) {
            actualMap.put(item.getSubjectCode(), item);
        }

        List<BudgetItem> items = new ArrayList<>();
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalActual = BigDecimal.ZERO;
        BigDecimal totalVariance = BigDecimal.ZERO;
        for (Budget b : budgets) {
            ReportItem actual = actualMap.get(b.getSubjectCode());
            BigDecimal actualAmount = BigDecimal.ZERO;
            boolean isDebit = true;
            String subjectName = null;
            if (actual != null) {
                subjectName = actual.getSubjectName();
                isDebit = DIRECTION_DEBIT.equals(actual.getBalanceDirection());
                actualAmount = isDebit
                        ? nz(actual.getPeriodDebit()).subtract(nz(actual.getPeriodCredit()))
                        : nz(actual.getPeriodCredit()).subtract(nz(actual.getPeriodDebit()));
            }
            BigDecimal variance = actualAmount.subtract(b.getBudgetAmount());
            boolean overBudget = isDebit
                    ? variance.compareTo(BigDecimal.ZERO) > 0   // 费用超支
                    : variance.compareTo(BigDecimal.ZERO) < 0;  // 收入未达标
            if (onlyOverBudget && !overBudget) {
                continue;
            }
            BudgetItem item = new BudgetItem();
            item.setSubjectCode(b.getSubjectCode());
            item.setSubjectName(subjectName);
            item.setBudgetAmount(b.getBudgetAmount());
            item.setActualAmount(actualAmount);
            item.setVariance(variance);
            item.setOverBudget(overBudget);
            items.add(item);
            totalBudget = totalBudget.add(b.getBudgetAmount());
            totalActual = totalActual.add(actualAmount);
            totalVariance = totalVariance.add(variance);
        }

        BudgetResponse response = new BudgetResponse();
        response.setPeriod(period);
        response.setItems(items);
        response.setTotalBudget(totalBudget);
        response.setTotalActual(totalActual);
        response.setTotalVariance(totalVariance);
        log.info("预算查询: 期间={}, 预算合计={}, 实际合计={}, 差异={}",
                period, totalBudget, totalActual, totalVariance);
        return response;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
