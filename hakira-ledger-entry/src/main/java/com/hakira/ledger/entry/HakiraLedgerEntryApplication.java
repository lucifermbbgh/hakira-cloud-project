package com.hakira.ledger.entry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.hakira.*")// 使用Feign客户端调用远程服务的注解
@ComponentScan(basePackages = {"com.hakira.ledger.entry", "com.hakira.common"})
public class HakiraLedgerEntryApplication {
    public static void main(String[] args) {
        SpringApplication.run(HakiraLedgerEntryApplication.class, args);
    }
}
