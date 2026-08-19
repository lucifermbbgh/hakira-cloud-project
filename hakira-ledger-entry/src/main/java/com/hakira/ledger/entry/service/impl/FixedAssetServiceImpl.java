package com.hakira.ledger.entry.service.impl;

import com.hakira.common.exception.BizErrorCode;
import com.hakira.common.exception.BizException;
import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.api.asset.IFixedAssetService;
import com.hakira.ledger.api.dto.asset.DepreciationResponse;
import com.hakira.ledger.api.dto.asset.DisposeResponse;
import com.hakira.ledger.api.dto.asset.FixedAssetRequest;
import com.hakira.ledger.api.dto.asset.FixedAssetResponse;
import com.hakira.ledger.entry.mapper.AccountingPeriodMapper;
import com.hakira.ledger.entry.mapper.FixedAssetMapper;
import com.hakira.ledger.entry.mapper.JournalEntryLineMapper;
import com.hakira.ledger.entry.mapper.JournalEntryMapper;
import com.hakira.ledger.entry.pojo.AccountingPeriod;
import com.hakira.ledger.entry.pojo.FixedAsset;
import com.hakira.ledger.entry.pojo.JournalEntry;
import com.hakira.ledger.entry.pojo.JournalEntryLine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 固定资产服务实现（资产卡片 / 折旧计提 / 资产处置）
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FixedAssetServiceImpl implements IFixedAssetService {

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DateTimeFormatter VOUCHER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String STATUS_IN_USE = "IN_USE";
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_POSTED = "POSTED";
    private static final String METHOD_DOUBLE_DECLINING = "DOUBLE_DECLINING";
    private static final String SUBJECT_FIXED_ASSET = "1601";
    private static final String SUBJECT_ACCUM_DEPRECIATION = "1602";
    private static final String SUBJECT_ADMIN_EXPENSE = "6602";
    private static final String SUBJECT_NON_OPERATING_EXPENSE = "6711";

    private final FixedAssetMapper fixedAssetMapper;
    private final JournalEntryMapper journalEntryMapper;
    private final JournalEntryLineMapper journalEntryLineMapper;
    private final AccountingPeriodMapper accountingPeriodMapper;

    @Override
    public FixedAssetResponse create(FixedAssetRequest request) {
        FixedAsset asset = new FixedAsset();
        asset.setAssetCode(request.getAssetCode());
        asset.setAssetName(request.getAssetName());
        asset.setCategory(request.getCategory());
        asset.setOriginalValue(request.getOriginalValue());
        asset.setResidualRate(request.getResidualRate() != null ? request.getResidualRate() : new BigDecimal("0.05"));
        asset.setUsefulLife(request.getUsefulLife());
        asset.setDepreciationMethod(request.getDepreciationMethod() != null
                ? request.getDepreciationMethod() : "STRAIGHT_LINE");
        asset.setPurchaseDate(request.getPurchaseDate());
        asset.setAccumulatedDepreciation(BigDecimal.ZERO);
        asset.setNetValue(request.getOriginalValue());
        asset.setStatus(STATUS_IN_USE);
        fixedAssetMapper.insert(asset);
        log.info("登记固定资产: {} {} 原值={}", asset.getAssetCode(), asset.getAssetName(), asset.getOriginalValue());
        return toResponse(asset);
    }

    @Override
    public FixedAssetResponse get(String assetCode) {
        FixedAsset asset = fixedAssetMapper.selectByCode(assetCode);
        if (asset == null) {
            throw new BizException(BizErrorCode.BIZ_ERROR, "资产不存在: " + assetCode);
        }
        return toResponse(asset);
    }

    @Override
    public List<FixedAssetResponse> list() {
        return fixedAssetMapper.selectAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepreciationResponse depreciate(String period) {
        // 期间校验（OPEN 才能计提）
        AccountingPeriod ap = accountingPeriodMapper.selectByPeriod(period);
        if (ap != null && !STATUS_OPEN.equals(ap.getStatus())) {
            throw new BizException(BizErrorCode.PERIOD_STATUS_INVALID,
                    String.format("期间 %s 状态为 %s，不允许折旧计提", period, ap.getStatus()));
        }
        LocalDate depreciateDate = YearMonth.parse(period, PERIOD_FORMATTER).atDay(1).plusMonths(1).minusDays(1);

        int count = 0;
        BigDecimal total = BigDecimal.ZERO;
        List<String> voucherNos = new ArrayList<>();
        for (FixedAsset asset : fixedAssetMapper.selectInUse()) {
            BigDecimal monthly = calcMonthlyDepreciation(asset);
            if (monthly.compareTo(BigDecimal.ZERO) <= 0) {
                continue; // 已提足折旧
            }
            // 生成折旧凭证：借 6602 管理费用 / 贷 1602 累计折旧
            voucherNos.add(writeDepreciationVoucher(asset, monthly, depreciateDate));
            // 更新累计折旧 + 净值（乐观锁）
            BigDecimal newAccumulated = nz(asset.getAccumulatedDepreciation()).add(monthly);
            BigDecimal newNetValue = asset.getNetValue().subtract(monthly);
            int updated = fixedAssetMapper.updateDepreciation(
                    asset.getAssetCode(), newAccumulated, newNetValue, asset.getVersion());
            if (updated == 0) {
                throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, asset.getAssetCode());
            }
            count++;
            total = total.add(monthly);
        }

        DepreciationResponse response = new DepreciationResponse();
        response.setPeriod(period);
        response.setDepreciatedCount(count);
        response.setTotalDepreciation(total);
        response.setVoucherNos(voucherNos);
        log.info("折旧计提: 期间={}, 资产数={}, 总折旧={}", period, count, total);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DisposeResponse dispose(String assetCode) {
        FixedAsset asset = fixedAssetMapper.selectByCode(assetCode);
        if (asset == null) {
            throw new BizException(BizErrorCode.BIZ_ERROR, "资产不存在: " + assetCode);
        }
        if (!STATUS_IN_USE.equals(asset.getStatus())) {
            throw new BizException(BizErrorCode.BIZ_ERROR, "资产非使用中状态，不可处置: " + assetCode);
        }
        BigDecimal netValue = asset.getNetValue();
        LocalDate disposeDate = LocalDate.now();
        String voucherNo = writeDisposeVoucher(asset, netValue, disposeDate);
        int updated = fixedAssetMapper.updateStatusDisposed(assetCode, asset.getVersion());
        if (updated == 0) {
            throw new BizException(BizErrorCode.DATA_VERSION_CONFLICT, assetCode);
        }
        log.info("处置固定资产: {} 净值={} 凭证={}", assetCode, netValue, voucherNo);
        DisposeResponse response = new DisposeResponse();
        response.setAssetCode(assetCode);
        response.setVoucherNo(voucherNo);
        response.setNetValue(netValue);
        return response;
    }

    /** 月折旧额（直线法 / 双倍余额递减法，不低于残值） */
    private BigDecimal calcMonthlyDepreciation(FixedAsset asset) {
        BigDecimal residualValue = asset.getOriginalValue().multiply(asset.getResidualRate());
        BigDecimal monthly;
        if (METHOD_DOUBLE_DECLINING.equals(asset.getDepreciationMethod())) {
            monthly = asset.getNetValue().multiply(BigDecimal.valueOf(2))
                    .divide(BigDecimal.valueOf(asset.getUsefulLife()), 2, RoundingMode.HALF_UP);
        } else {
            monthly = asset.getOriginalValue().multiply(BigDecimal.ONE.subtract(asset.getResidualRate()))
                    .divide(BigDecimal.valueOf(asset.getUsefulLife()), 2, RoundingMode.HALF_UP);
        }
        // 净值不低于残值
        BigDecimal newNetValue = asset.getNetValue().subtract(monthly);
        if (newNetValue.compareTo(residualValue) < 0) {
            monthly = asset.getNetValue().subtract(residualValue);
        }
        return monthly;
    }

    private String writeDepreciationVoucher(FixedAsset asset, BigDecimal monthly, LocalDate date) {
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(date);
        JournalEntry entry = new JournalEntry();
        entry.setEntryId(entryId);
        entry.setEntryDate(date);
        entry.setVoucherNo(voucherNo);
        entry.setDescription("计提折旧-" + asset.getAssetName());
        entry.setTotalDebit(monthly);
        entry.setTotalCredit(monthly);
        entry.setStatus(STATUS_POSTED);
        journalEntryMapper.insert(entry);

        insertLine(entryId, date, 1, SUBJECT_ADMIN_EXPENSE, "管理费用", "计提折旧", monthly, BigDecimal.ZERO);
        insertLine(entryId, date, 2, SUBJECT_ACCUM_DEPRECIATION, "累计折旧", "计提折旧", BigDecimal.ZERO, monthly);
        return voucherNo;
    }

    private String writeDisposeVoucher(FixedAsset asset, BigDecimal netValue, LocalDate date) {
        BigDecimal original = asset.getOriginalValue();
        BigDecimal accumulated = nz(asset.getAccumulatedDepreciation());
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(date);
        JournalEntry entry = new JournalEntry();
        entry.setEntryId(entryId);
        entry.setEntryDate(date);
        entry.setVoucherNo(voucherNo);
        entry.setDescription("处置固定资产-" + asset.getAssetName());
        entry.setTotalDebit(original);
        entry.setTotalCredit(original);
        entry.setStatus(STATUS_POSTED);
        journalEntryMapper.insert(entry);

        insertLine(entryId, date, 1, SUBJECT_ACCUM_DEPRECIATION, "累计折旧", "处置固定资产", accumulated, BigDecimal.ZERO);
        insertLine(entryId, date, 2, SUBJECT_NON_OPERATING_EXPENSE, "营业外支出", "处置固定资产", netValue, BigDecimal.ZERO);
        insertLine(entryId, date, 3, SUBJECT_FIXED_ASSET, "固定资产", "处置固定资产", BigDecimal.ZERO, original);
        return voucherNo;
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

    private FixedAssetResponse toResponse(FixedAsset asset) {
        FixedAssetResponse r = new FixedAssetResponse();
        r.setAssetCode(asset.getAssetCode());
        r.setAssetName(asset.getAssetName());
        r.setCategory(asset.getCategory());
        r.setOriginalValue(asset.getOriginalValue());
        r.setResidualRate(asset.getResidualRate());
        r.setUsefulLife(asset.getUsefulLife());
        r.setDepreciationMethod(asset.getDepreciationMethod());
        r.setAccumulatedDepreciation(asset.getAccumulatedDepreciation());
        r.setNetValue(asset.getNetValue());
        r.setStatus(asset.getStatus());
        r.setPurchaseDate(asset.getPurchaseDate());
        return r;
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
