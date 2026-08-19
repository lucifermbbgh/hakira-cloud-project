package com.hakira.ledger.entry.service.impl;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.audit.IAuditService;
import com.hakira.ledger.api.closing.IClosingService;
import com.hakira.ledger.api.dto.closing.AccountBalanceResponse;
import com.hakira.ledger.api.dto.closing.ClosingResponse;
import com.hakira.ledger.api.dto.closing.TrialBalanceResponse;
import com.hakira.ledger.entry.mapper.AccountBalanceMapper;
import com.hakira.ledger.entry.mapper.AccountingPeriodMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineMapper;
import com.hakira.ledger.entry.mapper.JournalEntryMapper;
import com.hakira.ledger.entry.pojo.AccountBalance;
import com.hakira.ledger.entry.pojo.AccountingPeriod;
import com.hakira.ledger.entry.pojo.JournalEntry;
import com.hakira.ledger.entry.pojo.JournalEntryLine;
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
 * 期末结账服务实现（损益结转 / 月结 / 试算平衡 / 科目余额）
 * <p>
 * 高并发考量（详见设计文档）：
 * 1. 余额物化：结账时一次性算好期末余额，查询走物化值不扫流水
 * 2. 聚合下推：SUM/GROUP BY 在数据库端聚合，不把全量行拉回应用层
 * 3. 分区裁剪：所有聚合查询带 entry_date 范围，命中按月分区
 * 4. 期间锁定：accounting_period 乐观锁 + 状态机，防止结账期间并发录入
 * 5. 结转幂等：已结转期间拒绝重复结转
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClosingServiceImpl implements IClosingService {

    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_CLOSING = "CLOSING";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String STATUS_POSTED = "POSTED";
    private static final String SUBJECT_PROFIT = "4103";
    private static final String DIRECTION_DEBIT = "D";
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter VOUCHER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AccountingPeriodMapper accountingPeriodMapper;
    private final AccountBalanceMapper accountBalanceMapper;
    private final JournalEntryMapper journalEntryMapper;
    private final JournalEntryLineMapper journalEntryLineMapper;
    private final IAuditService auditService;

    /** 结转科目（净额） */
    private static class TransferItem {
        final String subjectCode;
        final String subjectName;
        final BigDecimal amount;

        TransferItem(String subjectCode, String subjectName, BigDecimal amount) {
            this.subjectCode = subjectCode;
            this.subjectName = subjectName;
            this.amount = amount;
        }
    }

    @Override
    public List<AccountBalanceResponse> getBalance(String period) {
        // 已物化：直接读物化值（O(1)，不扫流水）
        List<AccountBalance> materialized = accountBalanceMapper.selectByPeriod(period);
        if (!materialized.isEmpty()) {
            List<AccountBalanceResponse> result = new ArrayList<>();
            for (AccountBalance b : materialized) {
                result.add(fromMaterialized(b));
            }
            return result;
        }
        // 未结账：实时聚合（期初=0，期末=本期发生归约）
        LocalDate start = parsePeriodStart(period);
        LocalDate end = start.plusMonths(1);
        List<AccountBalance> aggregated = accountBalanceMapper.aggregateByPeriod(start, end);
        List<AccountBalanceResponse> result = new ArrayList<>();
        for (AccountBalance a : aggregated) {
            result.add(fromAggregate(a, BigDecimal.ZERO, BigDecimal.ZERO));
        }
        return result;
    }

    @Override
    public TrialBalanceResponse getTrialBalance(String period) {
        LocalDate start = parsePeriodStart(period);
        LocalDate end = start.plusMonths(1);
        List<AccountBalance> aggregated = accountBalanceMapper.aggregateByPeriod(start, end);

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<AccountBalanceResponse> details = new ArrayList<>();
        for (AccountBalance a : aggregated) {
            totalDebit = totalDebit.add(nz(a.getPeriodDebit()));
            totalCredit = totalCredit.add(nz(a.getPeriodCredit()));
            details.add(fromAggregate(a, BigDecimal.ZERO, BigDecimal.ZERO));
        }
        BigDecimal difference = totalDebit.subtract(totalCredit);

        TrialBalanceResponse response = new TrialBalanceResponse();
        response.setBalanced(difference.compareTo(BigDecimal.ZERO) == 0);
        response.setTotalDebit(totalDebit);
        response.setTotalCredit(totalCredit);
        response.setDifference(difference);
        response.setDetails(details);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingResponse profitTransfer(String period) {
        checkPeriodOpen(period);
        return doProfitTransfer(period);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClosingResponse close(String period) {
        // 0. 结账顺序约束：上一期间必须先结账（期初承接依赖上一期间期末余额）
        String prev = prevPeriod(period);
        AccountingPeriod prevPeriod = accountingPeriodMapper.selectByPeriod(prev);
        if (prevPeriod != null && !STATUS_CLOSED.equals(prevPeriod.getStatus())) {
            throw new BizException(BizErrorCode.PERIOD_STATUS_INVALID,
                    String.format("上一期间 %s 未结账，请先结账上一期间", prev));
        }

        // 1. OPEN → CLOSING（乐观锁，防止结账期间并发录入）
        AccountingPeriod p = getOrCreatePeriod(period);
        if (!STATUS_OPEN.equals(p.getStatus())) {
            throw new BizException(BizErrorCode.PERIOD_STATUS_INVALID,
                    String.format("期间 %s 状态为 %s，不可结账", period, p.getStatus()));
        }
        int updated = accountingPeriodMapper.updateStatus(period, STATUS_CLOSING, p.getVersion());
        if (updated == 0) {
            throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, period);
        }

        // 2. 损益结转（未结转则执行；已单独结转则跳过复用）
        LocalDate start = parsePeriodStart(period);
        LocalDate end = start.plusMonths(1);
        ClosingResponse transferResult;
        if (accountBalanceMapper.countTransferEntries(start, end) == 0) {
            transferResult = doProfitTransfer(period);
        } else {
            transferResult = new ClosingResponse();
            transferResult.setVoucherNos(new ArrayList<>());
            transferResult.setTransferCount(0);
        }

        // 3. 物化全科目余额（含期初承接）
        int balanceCount = materializeBalance(period);

        // 4. CLOSING → CLOSED
        AccountingPeriod closing = accountingPeriodMapper.selectByPeriod(period);
        accountingPeriodMapper.updateStatus(period, STATUS_CLOSED, closing.getVersion());

        ClosingResponse response = new ClosingResponse();
        response.setPeriod(period);
        response.setStatus(STATUS_CLOSED);
        response.setVoucherNos(transferResult.getVoucherNos());
        response.setTransferCount(transferResult.getTransferCount());
        response.setBalanceCount(balanceCount);
        log.info("月结完成: 期间={}, 结转凭证={}, 物化科目={}", period,
                transferResult.getVoucherNos(), balanceCount);
        auditService.record("CLOSE_PERIOD", "PERIOD", period, "结账完成");
        return response;
    }

    /** 核心损益结转逻辑（不含期间校验，供 profitTransfer / close 复用） */
    private ClosingResponse doProfitTransfer(String period) {
        LocalDate start = parsePeriodStart(period);
        LocalDate end = start.plusMonths(1);
        LocalDate transferDate = end.minusDays(1); // 结转凭证记账日 = 期间末日

        // 幂等检查：已结转则拒绝重复结转
        if (accountBalanceMapper.countTransferEntries(start, end) > 0) {
            throw new BizException(BizErrorCode.PERIOD_STATUS_INVALID,
                    String.format("期间 %s 已存在结转凭证，请勿重复结转", period));
        }

        List<AccountBalance> pl = accountBalanceMapper.aggregateProfitLoss(start, end);
        List<TransferItem> incomes = new ArrayList<>();
        List<TransferItem> expenses = new ArrayList<>();
        for (AccountBalance a : pl) {
            BigDecimal net;
            if (DIRECTION_DEBIT.equals(a.getBalanceDirection())) {
                // 费用类：净借方余额 = 借 - 贷
                net = nz(a.getPeriodDebit()).subtract(nz(a.getPeriodCredit()));
                if (net.compareTo(BigDecimal.ZERO) > 0) {
                    expenses.add(new TransferItem(a.getSubjectCode(), a.getSubjectName(), net));
                }
            } else {
                // 收入类：净贷方余额 = 贷 - 借
                net = nz(a.getPeriodCredit()).subtract(nz(a.getPeriodDebit()));
                if (net.compareTo(BigDecimal.ZERO) > 0) {
                    incomes.add(new TransferItem(a.getSubjectCode(), a.getSubjectName(), net));
                }
            }
        }

        List<String> voucherNos = new ArrayList<>();
        if (!incomes.isEmpty()) {
            voucherNos.add(transferIncome(transferDate, incomes));
        }
        if (!expenses.isEmpty()) {
            voucherNos.add(transferExpense(transferDate, expenses));
        }

        ClosingResponse response = new ClosingResponse();
        response.setPeriod(period);
        response.setStatus(accountingPeriodMapper.selectByPeriod(period).getStatus());
        response.setVoucherNos(voucherNos);
        response.setTransferCount(incomes.size() + expenses.size());
        return response;
    }

    /** 结转收入：多借（收入科目）+ 一贷（本年利润） */
    private String transferIncome(LocalDate transferDate, List<TransferItem> incomes) {
        BigDecimal total = incomes.stream().map(i -> i.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(transferDate);

        JournalEntry entry = buildEntry(entryId, transferDate, voucherNo, "结转收入类损益至本年利润", total);
        journalEntryMapper.insert(entry);

        int lineNo = 1;
        for (TransferItem i : incomes) {
            insertLine(entryId, transferDate, lineNo++, i.subjectCode, i.subjectName,
                    "结转收入", i.amount, BigDecimal.ZERO);
        }
        insertLine(entryId, transferDate, lineNo, SUBJECT_PROFIT, "本年利润",
                "结转收入", BigDecimal.ZERO, total);
        log.info("结转收入凭证: {} 金额={}", voucherNo, total);
        return voucherNo;
    }

    /** 结转费用：一借（本年利润）+ 多贷（费用科目） */
    private String transferExpense(LocalDate transferDate, List<TransferItem> expenses) {
        BigDecimal total = expenses.stream().map(i -> i.amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(transferDate);

        JournalEntry entry = buildEntry(entryId, transferDate, voucherNo, "结转费用类损益至本年利润", total);
        journalEntryMapper.insert(entry);

        insertLine(entryId, transferDate, 1, SUBJECT_PROFIT, "本年利润",
                "结转费用", total, BigDecimal.ZERO);
        int lineNo = 2;
        for (TransferItem i : expenses) {
            insertLine(entryId, transferDate, lineNo++, i.subjectCode, i.subjectName,
                    "结转费用", BigDecimal.ZERO, i.amount);
        }
        log.info("结转费用凭证: {} 金额={}", voucherNo, total);
        return voucherNo;
    }

    private JournalEntry buildEntry(String entryId, LocalDate date, String voucherNo,
                                    String description, BigDecimal total) {
        JournalEntry entry = new JournalEntry();
        entry.setEntryId(entryId);
        entry.setEntryDate(date);
        entry.setVoucherNo(voucherNo);
        entry.setDescription(description);
        entry.setTotalDebit(total);
        entry.setTotalCredit(total);
        entry.setStatus(STATUS_POSTED);
        return entry;
    }

    private void insertLine(String entryId, LocalDate date, int lineNo, String subjectCode,
                            String subjectName, String description, BigDecimal debit, BigDecimal credit) {
        JournalEntryLine line = new JournalEntryLine();
        line.setEntryId(entryId);
        line.setEntryDate(date);
        line.setLineNo(lineNo);
        line.setSubjectCode(subjectCode);
        line.setSubjectName(subjectName);
        line.setDescription(description);
        line.setDebitAmount(debit);
        line.setCreditAmount(credit);
        journalEntryLineMapper.insert(line);
    }

    /** 物化全科目余额（期初承接 + 本期发生 + 期末归约） */
    private int materializeBalance(String period) {
        LocalDate start = parsePeriodStart(period);
        LocalDate end = start.plusMonths(1);
        List<AccountBalance> aggregated = accountBalanceMapper.aggregateByPeriod(start, end);

        // 上一期间期末余额（期初承接）
        Map<String, AccountBalance> prevMap = new HashMap<>();
        for (AccountBalance p : accountBalanceMapper.selectByPeriod(prevPeriod(period))) {
            prevMap.put(p.getSubjectCode(), p);
        }

        // 清理旧余额后重新物化
        accountBalanceMapper.deleteByPeriod(period);

        int count = 0;
        for (AccountBalance a : aggregated) {
            AccountBalance prev = prevMap.get(a.getSubjectCode());
            BigDecimal openingDebit = prev != null ? nz(prev.getClosingDebit()) : BigDecimal.ZERO;
            BigDecimal openingCredit = prev != null ? nz(prev.getClosingCredit()) : BigDecimal.ZERO;
            insertBalance(period, a.getSubjectCode(), a.getSubjectName(),
                    openingDebit, openingCredit, nz(a.getPeriodDebit()), nz(a.getPeriodCredit()));
            count++;
        }
        // 期初有余额但本期无发生的科目：结转期末=期初
        for (AccountBalance prev : prevMap.values()) {
            boolean hasCurrent = aggregated.stream()
                    .anyMatch(a -> a.getSubjectCode().equals(prev.getSubjectCode()));
            if (!hasCurrent) {
                insertBalance(period, prev.getSubjectCode(), prev.getSubjectName(),
                        nz(prev.getClosingDebit()), nz(prev.getClosingCredit()),
                        BigDecimal.ZERO, BigDecimal.ZERO);
                count++;
            }
        }
        return count;
    }

    private void insertBalance(String period, String subjectCode, String subjectName,
                               BigDecimal openingDebit, BigDecimal openingCredit,
                               BigDecimal periodDebit, BigDecimal periodCredit) {
        BigDecimal net = openingDebit.subtract(openingCredit)
                .add(periodDebit).subtract(periodCredit);
        AccountBalance balance = new AccountBalance();
        balance.setPeriod(period);
        balance.setSubjectCode(subjectCode);
        balance.setSubjectName(subjectName);
        balance.setOpeningDebit(openingDebit);
        balance.setOpeningCredit(openingCredit);
        balance.setPeriodDebit(periodDebit);
        balance.setPeriodCredit(periodCredit);
        balance.setClosingDebit(net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO);
        balance.setClosingCredit(net.compareTo(BigDecimal.ZERO) < 0 ? net.negate() : BigDecimal.ZERO);
        accountBalanceMapper.insertBalance(balance);
    }

    private AccountBalanceResponse fromMaterialized(AccountBalance b) {
        AccountBalanceResponse r = new AccountBalanceResponse();
        r.setSubjectCode(b.getSubjectCode());
        r.setSubjectName(b.getSubjectName());
        r.setOpeningDebit(nz(b.getOpeningDebit()));
        r.setOpeningCredit(nz(b.getOpeningCredit()));
        r.setPeriodDebit(nz(b.getPeriodDebit()));
        r.setPeriodCredit(nz(b.getPeriodCredit()));
        r.setClosingDebit(nz(b.getClosingDebit()));
        r.setClosingCredit(nz(b.getClosingCredit()));
        return r;
    }

    private AccountBalanceResponse fromAggregate(AccountBalance a, BigDecimal openingDebit,
                                                 BigDecimal openingCredit) {
        BigDecimal net = openingDebit.subtract(openingCredit)
                .add(nz(a.getPeriodDebit())).subtract(nz(a.getPeriodCredit()));
        AccountBalanceResponse r = new AccountBalanceResponse();
        r.setSubjectCode(a.getSubjectCode());
        r.setSubjectName(a.getSubjectName());
        r.setOpeningDebit(openingDebit);
        r.setOpeningCredit(openingCredit);
        r.setPeriodDebit(nz(a.getPeriodDebit()));
        r.setPeriodCredit(nz(a.getPeriodCredit()));
        r.setClosingDebit(net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO);
        r.setClosingCredit(net.compareTo(BigDecimal.ZERO) < 0 ? net.negate() : BigDecimal.ZERO);
        return r;
    }

    private void checkPeriodOpen(String period) {
        AccountingPeriod p = getOrCreatePeriod(period);
        if (!STATUS_OPEN.equals(p.getStatus())) {
            throw new BizException(BizErrorCode.PERIOD_STATUS_INVALID,
                    String.format("期间 %s 状态为 %s，不允许此操作", period, p.getStatus()));
        }
    }

    private AccountingPeriod getOrCreatePeriod(String period) {
        AccountingPeriod p = accountingPeriodMapper.selectByPeriod(period);
        if (p == null) {
            accountingPeriodMapper.insertOpen(period);
            p = accountingPeriodMapper.selectByPeriod(period);
        }
        return p;
    }

    /** 凭证号：PZ-YYYYMMDD-NNN 自动编号 */
    private String generateVoucherNo(LocalDate entryDate) {
        String dateStr = entryDate.format(VOUCHER_DATE_FORMATTER);
        String maxVoucherNo = journalEntryMapper.selectMaxVoucherNo(dateStr);
        int seq = 1;
        if (maxVoucherNo != null && !maxVoucherNo.isEmpty()) {
            int idx = maxVoucherNo.lastIndexOf('-');
            if (idx >= 0) {
                try {
                    seq = Integer.parseInt(maxVoucherNo.substring(idx + 1)) + 1;
                } catch (NumberFormatException ignored) {
                    // 非数字后缀，从 1 开始
                }
            }
        }
        return String.format("PZ-%s-%03d", dateStr, seq);
    }

    private LocalDate parsePeriodStart(String period) {
        return YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
    }

    private String prevPeriod(String period) {
        return YearMonth.parse(period, PERIOD_FORMATTER).minusMonths(1).format(PERIOD_FORMATTER);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
