package com.hakira.ledger.stock.service;

import com.hakira.common.util.IdGeneratorUtil;
import com.hakira.ledger.stock.mapper.CostTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 出库成本结转服务：出库时生成结转凭证（借 6401 主营业务成本 / 贷 1405 库存商品）
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CostTransferService {

    private static final DateTimeFormatter VOUCHER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CostTransferMapper costTransferMapper;

    /** 出库成本结转，返回凭证号 */
    public String transferOutboundCost(String itemCode, String itemName, BigDecimal cost, LocalDate date) {
        String entryId = IdGeneratorUtil.getId();
        String voucherNo = generateVoucherNo(date);
        costTransferMapper.insertEntry(entryId, date, voucherNo, "出库成本结转-" + itemName, cost);
        costTransferMapper.insertLine(entryId, date, 1, "6401", "主营业务成本",
                "出库成本结转", cost, BigDecimal.ZERO);
        costTransferMapper.insertLine(entryId, date, 2, "1405", "库存商品",
                "出库成本结转", BigDecimal.ZERO, cost);
        log.info("出库成本结转: itemCode={}, 成本={}, 凭证={}", itemCode, cost, voucherNo);
        return voucherNo;
    }

    private String generateVoucherNo(LocalDate entryDate) {
        String dateStr = entryDate.format(VOUCHER_DATE_FORMATTER);
        String maxVoucherNo = costTransferMapper.selectMaxVoucherNo(dateStr);
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
}
