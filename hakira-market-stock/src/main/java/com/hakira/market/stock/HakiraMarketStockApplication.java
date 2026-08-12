package com.hakira.market.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.hakira.*")
public class HakiraMarketStockApplication {
    public static void main(String[] args) {
        SpringApplication.run(HakiraMarketStockApplication.class, args);
    }
}
