package com.hakira.ledger.api.aging;

import com.hakira.ledger.api.dto.aging.BadDebtProvisionResponse;
import com.hakira.ledger.api.dto.aging.BadDebtRequest;
import com.hakira.ledger.api.dto.aging.BadDebtResponse;

/**
 * 坏账处理服务接口 — 计提 / 核销 / 收回
 */
public interface IBadDebtService {
    /** 坏账准备计提（账龄分析法，生成计提凭证） */
    BadDebtProvisionResponse provision(String asOfDate);

    /** 坏账核销（借 1231 / 贷 1122） */
    BadDebtResponse writeoff(BadDebtRequest request);

    /** 坏账收回（恢复 + 收款两步分录） */
    BadDebtResponse recover(BadDebtRequest request);
}
