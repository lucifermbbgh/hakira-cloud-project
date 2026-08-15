package com.hakira.ledger.entry.service.impl;

import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.aging.IBadDebtService;
import com.hakira.ledger.api.dto.aging.BadDebtProvisionResponse;
import com.hakira.ledger.api.dto.aging.BadDebtRequest;
import com.hakira.ledger.api.dto.aging.BadDebtResponse;
import com.hakira.ledger.entry.mapper.AgingMapper;
import com.hakira.ledger.entry.mapper.BadDebtMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineAuxMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineMapper;
import com.hakira.ledger.entry.mapper.JournalEntryMapper;
import com.hakira.ledger.entry.pojo.AgingRow;
import com.hakira.ledger.entry.pojo.JournalEntry;
import com.hakira.ledger.entry.pojo.JournalEntryLine;
import com.hakira.ledger.entry.pojo.ReportItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 坏账处理服务实现（计提 / 核销 / 收回）
 * <p>
 * 备抵法：期末按账龄分析法计提坏账准备，确认坏账核销，已核销又收回。
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BadDebtServiceImpl implements IBadDebtService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter VOUCHER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String SUBJECT_BAD_DEBT = "1231";
    private static final String SUBJECT_RECEIVABLE = "1122";
    private static final String SUBJECT_IMPAIRMENT = "6701";
    private static final String SUBJECT_BANK = "1002";

    private final AgingMapper agingMapper;
    private final BadDebtMapper badDebtMapper;
    private final JournalEntryMapper journalEntryMapper;
    private final JournalEntryLineMapper journalEntryLineMapper;
    private final JournalEntryLineAuxMapper journalEntryLineAuxMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BadDebtProvisionResponse provision(String asOfDate) {
        LocalDate asOf = LocalDate.parse(asOfDate, DATE_FORMATTER);
        List<AgingRow> rows = agingMapper.selectAgingDetails("CUSTOMER", SUBJECT_RECEIVABLE, asOf);

        BigDecimal b1 = BigDecimal.ZERO, b2 = BigDecimal.ZERO, b3 = BigDecimal.ZERO, b4 = BigDecimal.ZERO;
        for (AgingRow row : rows) {
            BigDecimal net = nz(row.getDebitAmount()).subtract(nz(row.getCreditAmount()));
            if (net.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(row.getEntryDate(), asOf);
            if (days <= 365) {
                b1 = b1.add(net);
            } else if (days <= 730) {
                b2 = b2.add(net);
            } else if (days <= 1095) {
                b3 = b3.add(net);
            } else {
                b4 = b4.add(net);
            }
        }
        BigDecimal p1 = b1.multiply(new BigDecimal("0.05")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal p2 = b2.multiply(new BigDecimal("0.10")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal p3 = b3.multiply(new BigDecimal("0.30")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal p4 = b4.multiply(new BigDecimal("0.50")).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal totalProvision = p1.add(p2).add(p3).add(p4);

        ReportItem bd = badDebtMapper.selectBadDebtBalance(asOf);
        BigDecimal existing = bd != null
                ? nz(bd.getPeriodCredit()).subtract(nz(bd.getPeriodDebit()))
                : BigDecimal.ZERO;
        BigDecimal diff = totalProvision.subtract(existing);

        String voucherNo = null;
        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            voucherNo = writeProvisionVoucher(asOf, diff, true);   // 补提
        } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
            voucherNo = writeProvisionVoucher(asOf, diff.negate(), false); // 冲回
        }

        BadDebtProvisionResponse response = new BadDebtProvisionResponse();
        response.setAsOfDate(asOfDate);
        response.setProvisionAmount(diff);
        response.setVoucherNo(voucherNo);
        List<BadDebtProvisionResponse.AgeBucket> buckets = new ArrayList<>();
        buckets.add(bucket("1年内", b1, "0.05", p1));
        buckets.add(bucket("1-2年", b2, "0.10", p2));
        buckets.add(bucket("2-3年", b3, "0.30", p3));
        buckets.add(bucket("3年以上", b4, "0.50", p4));
        response.setBuckets(buckets);
        log.info("坏账计提: 截止={}, 应计提={}, 已计提={}, 差额={}, 凭证={}",
                asOfDate, totalProvision, existing, diff, voucherNo);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BadDebtResponse writeoff(BadDebtRequest request) {
        LocalDate date = LocalDate.now();
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(date);
        BigDecimal amount = request.getAmount();

        JournalEntry entry = buildEntry(entryId, date, voucherNo,
                "坏账核销-" + request.getCustomerCode(), amount);
        journalEntryMapper.insert(entry);
        insertLine(entryId, date, 1, SUBJECT_BAD_DEBT, "坏账准备", "坏账核销", amount, BigDecimal.ZERO);
        JournalEntryLine creditLine = insertLine(entryId, date, 2, SUBJECT_RECEIVABLE, "应收账款",
                "坏账核销", BigDecimal.ZERO, amount);
        journalEntryLineAuxMapper.insert(creditLine.getLineId(), "CUSTOMER", request.getCustomerCode());

        BadDebtResponse response = new BadDebtResponse();
        response.setVoucherNos(List.of(voucherNo));
        response.setAmount(amount);
        log.info("坏账核销: 客户={}, 金额={}, 凭证={}", request.getCustomerCode(), amount, voucherNo);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BadDebtResponse recover(BadDebtRequest request) {
        LocalDate date = LocalDate.now();
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(date);
        BigDecimal amount = request.getAmount();

        JournalEntry entry = buildEntry(entryId, date, voucherNo,
                "坏账收回-" + request.getCustomerCode(), amount);
        journalEntryMapper.insert(entry);
        // 恢复：借 1122 贷 1231
        JournalEntryLine restoreLine = insertLine(entryId, date, 1, SUBJECT_RECEIVABLE, "应收账款",
                "坏账收回恢复", amount, BigDecimal.ZERO);
        journalEntryLineAuxMapper.insert(restoreLine.getLineId(), "CUSTOMER", request.getCustomerCode());
        insertLine(entryId, date, 2, SUBJECT_BAD_DEBT, "坏账准备", "坏账收回恢复", BigDecimal.ZERO, amount);
        // 收款：借 1002 贷 1122
        insertLine(entryId, date, 3, SUBJECT_BANK, "银行存款", "坏账收回收款", amount, BigDecimal.ZERO);
        insertLine(entryId, date, 4, SUBJECT_RECEIVABLE, "应收账款", "坏账收回收款", BigDecimal.ZERO, amount);

        BadDebtResponse response = new BadDebtResponse();
        response.setVoucherNos(List.of(voucherNo));
        response.setAmount(amount);
        log.info("坏账收回: 客户={}, 金额={}, 凭证={}", request.getCustomerCode(), amount, voucherNo);
        return response;
    }

    private BadDebtProvisionResponse.AgeBucket bucket(String name, BigDecimal balance,
                                                      String rate, BigDecimal provision) {
        BadDebtProvisionResponse.AgeBucket b = new BadDebtProvisionResponse.AgeBucket();
        b.setBucket(name);
        b.setBalance(balance);
        b.setRate(new BigDecimal(rate));
        b.setProvision(provision);
        return b;
    }

    private String writeProvisionVoucher(LocalDate date, BigDecimal amount, boolean supplement) {
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(date);
        JournalEntry entry = buildEntry(entryId, date, voucherNo,
                supplement ? "坏账准备补提" : "坏账准备冲回", amount);
        journalEntryMapper.insert(entry);
        if (supplement) {
            insertLine(entryId, date, 1, SUBJECT_IMPAIRMENT, "资产减值损失", "坏账准备补提", amount, BigDecimal.ZERO);
            insertLine(entryId, date, 2, SUBJECT_BAD_DEBT, "坏账准备", "坏账准备补提", BigDecimal.ZERO, amount);
        } else {
            insertLine(entryId, date, 1, SUBJECT_BAD_DEBT, "坏账准备", "坏账准备冲回", amount, BigDecimal.ZERO);
            insertLine(entryId, date, 2, SUBJECT_IMPAIRMENT, "资产减值损失", "坏账准备冲回", BigDecimal.ZERO, amount);
        }
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
        entry.setStatus("POSTED");
        return entry;
    }

    private JournalEntryLine insertLine(String entryId, LocalDate date, int lineNo, String subjectCode,
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
        return line;
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
