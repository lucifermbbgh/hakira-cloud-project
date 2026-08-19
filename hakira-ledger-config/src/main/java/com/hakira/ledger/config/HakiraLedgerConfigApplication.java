package com.hakira.ledger.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HakiraLedgerConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(HakiraLedgerConfigApplication.class, args);
    }

}
