package com.hakira.fury.config;

import com.hakira.fury.pool.FuryPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.fury.config
 * @Author: hakiraKafka
 * @CreateTime: 2025-03-04  11:36:06
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
public class FuryConfiguration {
    @Bean
    public FuryPool furyPool() {
        return new FuryPool();
    }
}
