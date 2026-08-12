package com.hakira.fury.pool;

import com.hakira.fury.factory.FuryFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.fury.Fury;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.fury.pool
 * @Author: hakiraKafka
 * @CreateTime: 2025-03-04  11:34:56
 * @Description: TODO
 * @Version: 1.0
 */
public class FuryPool {
    private final GenericObjectPool<Fury> pool;

    public FuryPool() {
        // 配置对象池
        GenericObjectPoolConfig<Fury> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(10); // 最大对象数
        config.setMinIdle(2);   // 最小空闲对象数
        config.setMaxIdle(5);  // 最大空闲对象数
        config.setTestOnBorrow(true); // 借出时验证对象

        // 创建对象池
        pool = new GenericObjectPool<>(new FuryFactory(), config);
    }

    /**
     * 从池中借出 Fury 对象
     */
    public Fury borrowFury() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to borrow Fury object from pool", e);
        }
    }

    /**
     * 归还 Fury 对象到池中
     */
    public void returnFury(Fury fury) {
        pool.returnObject(fury);
    }

    /**
     * 关闭对象池
     */
    public void close() {
        pool.close();
    }
}
