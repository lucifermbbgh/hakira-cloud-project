package com.hakira.ledger.workflow.delegate;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AutoSignDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        String voucherNo = (String) execution.getVariable("voucherNo");
        log.info("自动签字完成: voucherNo={}", voucherNo);
        execution.setVariable("signStatus", "SIGNED");
    }
}
