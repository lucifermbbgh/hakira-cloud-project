package com.hakira.ledger.task;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class HakiraLedgerTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(HakiraLedgerTaskApplication.class, args);
    }

}
