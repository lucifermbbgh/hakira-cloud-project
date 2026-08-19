package com.hakira.ledger.api.cost;

import com.hakira.ledger.api.dto.cost.CostAllocateResponse;
import com.hakira.ledger.api.dto.cost.CostSheetResponse;

/**
 * 成本核算服务接口 — 制造费用分配 / 成本计算单
 */
public interface ICostService {
    /** 制造费用分配（5101 → 5001，生成分配凭证） */
    CostAllocateResponse allocateOverhead(String period);

    /** 成本计算单（料工费聚合） */
    CostSheetResponse getCostSheet(String period);
}
