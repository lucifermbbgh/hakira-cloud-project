package com.hakira.ledger.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.hakira.*")
public class HakiraLedgerStockApplication {
    public static void main(String[] args) {
        SpringApplication.run(HakiraLedgerStockApplication.class, args);
    }
}
