package com.hakira.ledger.task.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReconciliationScheduler {
    private final JobLauncher jobLauncher;
    private final Job reconciliationJob;

    /** 每日凌晨2点执行对账 */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runDailyReconciliation() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            log.info("定时触发日结对账...");
            jobLauncher.run(reconciliationJob, params);
        } catch (Exception e) {
            log.error("日结对账执行失败", e);
        }
    }
}
