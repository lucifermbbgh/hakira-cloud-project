package com.hakira.ledger.api.aging;

import com.hakira.ledger.api.dto.aging.AgingResponse;
import com.hakira.ledger.api.dto.aging.PartnerResponse;

import java.util.List;

/**
 * 往来账龄服务接口 — 往来单位 / 账龄分析
 */
public interface IAgingService {
    /** 往来单位列表（dimension=CUSTOMER/SUPPLIER） */
    List<PartnerResponse> listPartners(String dimension);

    /** 应收账款账龄 */
    AgingResponse getReceivableAging(String asOfDate);

    /** 应付账款账龄 */
    AgingResponse getPayableAging(String asOfDate);
}
