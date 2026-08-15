package com.hakira.ledger.entry.service.impl;

import com.hakira.ledger.api.aging.IAgingService;
import com.hakira.ledger.api.dto.aging.AgingItem;
import com.hakira.ledger.api.dto.aging.AgingResponse;
import com.hakira.ledger.api.dto.aging.PartnerResponse;
import com.hakira.ledger.entry.mapper.AgingMapper;
import com.hakira.ledger.entry.pojo.AgingRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 往来账龄服务实现（往来单位 / 应收应付账龄分析）
 * <p>
 * 复用 Phase 7 辅助核算维度（CUSTOMER/SUPPLIER）标识往来单位，账龄按发生日期分段。
 *
 * @author hakiraKafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AgingServiceImpl implements IAgingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DIM_CUSTOMER = "CUSTOMER";
    private static final String DIM_SUPPLIER = "SUPPLIER";
    private static final String SUBJECT_RECEIVABLE = "1122";
    private static final String SUBJECT_PAYABLE = "2202";

    private final AgingMapper agingMapper;

    @Override
    public List<PartnerResponse> listPartners(String dimension) {
        return agingMapper.selectPartners(dimension).stream().map(row -> {
            PartnerResponse p = new PartnerResponse();
            p.setValueCode(row.getValueCode());
            p.setValueName(row.getValueName());
            return p;
        }).collect(Collectors.toList());
    }

    @Override
    public AgingResponse getReceivableAging(String asOfDate) {
        return buildAging(asOfDate, DIM_CUSTOMER, SUBJECT_RECEIVABLE, "RECEIVABLE", true);
    }

    @Override
    public AgingResponse getPayableAging(String asOfDate) {
        return buildAging(asOfDate, DIM_SUPPLIER, SUBJECT_PAYABLE, "PAYABLE", false);
    }

    /** 账龄分析：按往来单位分组，净额按账龄段（30/60/90 天）归类 */
    private AgingResponse buildAging(String asOfDate, String dimension, String subjectCode,
                                     String type, boolean receivable) {
        LocalDate asOf = LocalDate.parse(asOfDate, DATE_FORMATTER);
        List<AgingRow> rows = agingMapper.selectAgingDetails(dimension, subjectCode, asOf);
        Map<String, List<AgingRow>> byPartner = rows.stream()
                .collect(Collectors.groupingBy(AgingRow::getValueCode, Collectors.toList()));

        List<AgingItem> items = new ArrayList<>();
        BigDecimal totalBalance = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;
        for (Map.Entry<String, List<AgingRow>> e : byPartner.entrySet()) {
            String partnerCode = e.getKey();
            String partnerName = e.getValue().get(0).getValueName();
            BigDecimal aging30 = BigDecimal.ZERO;
            BigDecimal aging60 = BigDecimal.ZERO;
            BigDecimal aging90 = BigDecimal.ZERO;
            BigDecimal agingOver90 = BigDecimal.ZERO;
            for (AgingRow row : e.getValue()) {
                BigDecimal net = receivable
                        ? nz(row.getDebitAmount()).subtract(nz(row.getCreditAmount()))
                        : nz(row.getCreditAmount()).subtract(nz(row.getDebitAmount()));
                long days = ChronoUnit.DAYS.between(row.getEntryDate(), asOf);
                if (days <= 30) {
                    aging30 = aging30.add(net);
                } else if (days <= 60) {
                    aging60 = aging60.add(net);
                } else if (days <= 90) {
                    aging90 = aging90.add(net);
                } else {
                    agingOver90 = agingOver90.add(net);
                }
            }
            BigDecimal total = aging30.add(aging60).add(aging90).add(agingOver90);
            AgingItem item = new AgingItem();
            item.setPartnerCode(partnerCode);
            item.setPartnerName(partnerName);
            item.setAging30(aging30);
            item.setAging60(aging60);
            item.setAging90(aging90);
            item.setAgingOver90(agingOver90);
            item.setTotalBalance(total);
            item.setOverdue(agingOver90.compareTo(BigDecimal.ZERO) > 0);
            items.add(item);
            totalBalance = totalBalance.add(total);
            totalOverdue = totalOverdue.add(agingOver90);
        }

        AgingResponse response = new AgingResponse();
        response.setAsOfDate(asOfDate);
        response.setType(type);
        response.setItems(items);
        response.setTotalBalance(totalBalance);
        response.setTotalOverdue(totalOverdue);
        log.info("账龄分析: 类型={}, 截止={}, 往来单位数={}, 总余额={}, 超龄={}",
                type, asOfDate, items.size(), totalBalance, totalOverdue);
        return response;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
