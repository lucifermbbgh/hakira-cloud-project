package com.hakira.club.article.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.club.article.config
 * @Author: hakiraKafka
 * @CreateTime: 2023-12-05  23:47:08
 * @Description: RestTemplate配置类
 * @Version: 1.0
 */
@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    public RestTemplate getTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate;
    }
}
