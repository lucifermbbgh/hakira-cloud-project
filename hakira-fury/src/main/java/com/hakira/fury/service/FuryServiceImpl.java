package com.hakira.fury.service;

import com.hakira.fury.pool.FuryPool;
import org.apache.fury.Fury;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.fury.service
 * @Author: hakiraKafka
 * @CreateTime: 2025-03-04  15:48:30
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class FuryServiceImpl {
    @Autowired
    FuryPool furyPool;

    public void getFury(){
        Fury fury = furyPool.borrowFury();
    }
}
