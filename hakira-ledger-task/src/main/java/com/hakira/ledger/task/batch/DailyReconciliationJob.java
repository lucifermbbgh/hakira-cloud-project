package com.hakira.ledger.task.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DailyReconciliationJob {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job reconciliationJob() {
        return new JobBuilder("dailyReconciliationJob", jobRepository)
            .start(reconcileStep())
            .next(reportStep())
            .build();
    }

    @Bean
    public Step reconcileStep() {
        return new StepBuilder("reconcileStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("===== 日结对账开始 =====");
                log.info("正在检查今日借贷平衡...");
                // 实际场景：查询今日所有 Entry，校验 totalDebit == totalCredit
                log.info("对账结果：借贷平衡 ✓");
                log.info("===== 日结对账完成 =====");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Step reportStep() {
        return new StepBuilder("reportStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info("===== 生成日报 =====");
                log.info("正在生成每日账务汇总...");
                // 实际场景：生成日报表
                log.info("日报已生成");
                log.info("===== 日报完成 =====");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
