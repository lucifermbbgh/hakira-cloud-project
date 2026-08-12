package com.hakira.ledger.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hakira.ledger.auth.mapper")
public class HakiraLedgerAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(HakiraLedgerAuthApplication.class, args);
    }

}
