package com.hakira.ledger.task.controller;

import com.hakira.common.pojo.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/task/reconciliation")
@Slf4j
@RequiredArgsConstructor
public class ReconciliationController {
    private final JobLauncher jobLauncher;
    private final Job reconciliationJob;

    @PostMapping("/run")
    public Result<Map<String, Object>> runNow() {
        try {
            JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            JobExecution execution = jobLauncher.run(reconciliationJob, params);

            Map<String, Object> result = Map.of(
                "jobName", execution.getJobInstance().getJobName(),
                "status", execution.getStatus().name(),
                "startTime", execution.getStartTime().toString()
            );
            return Result.returnSuccess(result);
        } catch (Exception e) {
            log.error("手动触发对账失败", e);
            return Result.returnFail("JOB_ERROR", e.getMessage());
        }
    }
}
