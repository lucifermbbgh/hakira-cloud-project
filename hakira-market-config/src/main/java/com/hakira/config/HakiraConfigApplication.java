package com.hakira.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HakiraConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(HakiraConfigApplication.class, args);
    }

}
