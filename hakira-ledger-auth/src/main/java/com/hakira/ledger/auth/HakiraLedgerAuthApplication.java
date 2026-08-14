package com.hakira.ledger.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.hakira.ledger.auth.mapper")
@ComponentScan(basePackages = {"com.hakira.ledger.auth", "com.hakira.common"})
public class HakiraLedgerAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(HakiraLedgerAuthApplication.class, args);
    }

}
