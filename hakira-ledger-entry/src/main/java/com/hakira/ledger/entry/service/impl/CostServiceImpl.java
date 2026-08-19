package com.hakira.ledger.entry.service.impl;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.cost.ICostService;
import com.hakira.ledger.api.dto.cost.CostAllocateResponse;
import com.hakira.ledger.api.dto.cost.CostSheetResponse;
import com.hakira.ledger.entry.mapper.AccountingPeriodMapper;
import com.hakira.ledger.entry.mapper.CostMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineAuxMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineMapper;
import com.hakira.ledger.entry.mapper.JournalEntryMapper;
import com.hakira.ledger.entry.pojo.AccountingPeriod;
import com.hakira.ledger.entry.pojo.JournalEntry;
import com.hakira.ledger.entry.pojo.JournalEntryLine;
import com.hakira.ledger.entry.pojo.ReportItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成本核算服务实现（制造费用分配 / 成本计算单）
 * <p>
 * 成本项目（料工费）复用 Phase 7 辅助核算框架：COST_ITEM 维度 + DM/DL/MO 三个值，
 * 成本归集分录的 5001/5101 行挂 COST_ITEM 维度，成本计算单按此聚合。
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CostServiceImpl implements ICostService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter VOUCHER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_POSTED = "POSTED";
    private static final String SUBJECT_PRODUCTION = "5001";
    private static final String SUBJECT_OVERHEAD = "5101";
    private static final String COST_DM = "DM";
    private static final String COST_DL = "DL";
    private static final String COST_MO = "MO";

    private final CostMapper costMapper;
    private final JournalEntryMapper journalEntryMapper;
    private final JournalEntryLineMapper journalEntryLineMapper;
    private final JournalEntryLineAuxMapper journalEntryLineAuxMapper;
    private final AccountingPeriodMapper accountingPeriodMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CostAllocateResponse allocateOverhead(String period) {
        // 1. 期间校验（OPEN 才能分配）
        checkPeriodOpen(period);
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        LocalDate allocateDate = end.minusDays(1);

        // 2. 制造费用净额（借 - 贷）
        ReportItem overhead = costMapper.selectOverheadByPeriod(start, end);
        BigDecimal net = nz(overhead.getPeriodDebit()).subtract(nz(overhead.getPeriodCredit()));
        if (net.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(BizErrorCode.BIZ_ERROR, "本期无制造费用可分配（5101 余额非借方正）");
        }

        // 3. 生成分配凭证：借 5001(MO) / 贷 5101
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(allocateDate);
        JournalEntry entry = new JournalEntry();
        entry.setEntryId(entryId);
        entry.setEntryDate(allocateDate);
        entry.setVoucherNo(voucherNo);
        entry.setDescription("制造费用分配至生产成本");
        entry.setTotalDebit(net);
        entry.setTotalCredit(net);
        entry.setStatus(STATUS_POSTED);
        journalEntryMapper.insert(entry);

        JournalEntryLine debitLine = new JournalEntryLine();
        debitLine.setEntryId(entryId);
        debitLine.setEntryDate(allocateDate);
        debitLine.setLineNo(1);
        debitLine.setSubjectCode(SUBJECT_PRODUCTION);
        debitLine.setSubjectName("生产成本");
        debitLine.setDescription("制造费用分配");
        debitLine.setDebitAmount(net);
        debitLine.setCreditAmount(BigDecimal.ZERO);
        journalEntryLineMapper.insert(debitLine);
        journalEntryLineAuxMapper.insert(debitLine.getLineId(), "COST_ITEM", COST_MO);

        JournalEntryLine creditLine = new JournalEntryLine();
        creditLine.setEntryId(entryId);
        creditLine.setEntryDate(allocateDate);
        creditLine.setLineNo(2);
        creditLine.setSubjectCode(SUBJECT_OVERHEAD);
        creditLine.setSubjectName("制造费用");
        creditLine.setDescription("制造费用分配");
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(net);
        journalEntryLineMapper.insert(creditLine);

        log.info("制造费用分配: 期间={}, 金额={}, 凭证={}", period, net, voucherNo);
        CostAllocateResponse response = new CostAllocateResponse();
        response.setPeriod(period);
        response.setAllocatedAmount(net);
        response.setVoucherNo(voucherNo);
        return response;
    }

    @Override
    public CostSheetResponse getCostSheet(String period) {
        LocalDate start = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1);
        LocalDate end = start.plusMonths(1);
        List<ReportItem> items = costMapper.selectCostByPeriod(start, end);
        Map<String, BigDecimal> net = new HashMap<>();
        for (ReportItem item : items) {
            BigDecimal amount = nz(item.getPeriodDebit()).subtract(nz(item.getPeriodCredit()));
            net.merge(item.getValueCode(), amount, BigDecimal::add);
        }
        BigDecimal dm = net.getOrDefault(COST_DM, BigDecimal.ZERO);
        BigDecimal dl = net.getOrDefault(COST_DL, BigDecimal.ZERO);
        BigDecimal mo = net.getOrDefault(COST_MO, BigDecimal.ZERO);
        CostSheetResponse response = new CostSheetResponse();
        response.setPeriod(period);
        response.setDirectMaterial(dm);
        response.setDirectLabor(dl);
        response.setManufacturingOverhead(mo);
        response.setTotalCost(dm.add(dl).add(mo));
        log.info("成本计算单: 期间={}, 材料={}, 人工={}, 制造费用={}, 总成本={}",
                period, dm, dl, mo, response.getTotalCost());
        return response;
    }

    private void checkPeriodOpen(String period) {
        AccountingPeriod p = accountingPeriodMapper.selectByPeriod(period);
        if (p != null && !STATUS_OPEN.equals(p.getStatus())) {
            throw new BizException(BizErrorCode.PERIOD_STATUS_INVALID,
                    String.format("期间 %s 状态为 %s，不允许制造费用分配", period, p.getStatus()));
        }
    }

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

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
