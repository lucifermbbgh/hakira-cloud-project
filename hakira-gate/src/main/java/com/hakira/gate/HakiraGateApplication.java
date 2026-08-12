package com.hakira.gate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hakira.gate.mapper")
public class HakiraGateApplication {

    public static void main(String[] args) {
        SpringApplication.run(HakiraGateApplication.class, args);
    }

}
