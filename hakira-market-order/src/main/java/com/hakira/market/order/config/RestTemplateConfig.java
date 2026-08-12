package com.hakira.market.order.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.config
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-17  17:51:08
 * @Description: RestTemplate配置类
 * @Version: 1.0
 */
@Configuration
public class RestTemplateConfig {
    // 获取RestTemplate属性
    @Bean
    // 设置负载均衡
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate;
    }
}
