package com.hakira.ledger.api.closing;

import com.hakira.ledger.api.dto.closing.AccountBalanceResponse;
import com.hakira.ledger.api.dto.closing.ClosingResponse;
import com.hakira.ledger.api.dto.closing.TrialBalanceResponse;

import java.util.List;

/**
 * 期末结账服务接口 — 损益结转 / 月结 / 试算平衡 / 科目余额
 */
public interface IClosingService {
    /** 科目余额表（期初/本期发生/期末） */
    List<AccountBalanceResponse> getBalance(String period);

    /** 试算平衡表 */
    TrialBalanceResponse getTrialBalance(String period);

    /** 损益结转（生成结转凭证） */
    ClosingResponse profitTransfer(String period);

    /** 月结（损益结转 + 余额物化 + 期间锁定） */
    ClosingResponse close(String period);
}
