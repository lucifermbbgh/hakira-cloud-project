package com.hakira.ledger.workflow.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LedgerValidationDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        // 获取流程变量
        String voucherNo = (String) execution.getVariable("voucherNo");
        log.info("校验记账凭证: voucherNo={}", voucherNo);
        // 校验借贷平衡（此处为演示，实际可调 IEntryService）
        execution.setVariable("validationResult", "PASSED");
    }
}
