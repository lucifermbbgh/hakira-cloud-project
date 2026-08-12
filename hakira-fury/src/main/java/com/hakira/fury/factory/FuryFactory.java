package com.hakira.fury.factory;

import com.hakira.common.pojo.common.Result;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.fury.factory
 * @Author: hakiraKafka
 * @CreateTime: 2025-03-04  11:35:25
 * @Description: TODO
 * @Version: 1.0
 */
public class FuryFactory extends BasePooledObjectFactory<Fury> {
    @Override
    public Fury create() {
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        // 注册需要序列化的类
        fury.register(Result.class);
        return fury;
    }

    @Override
    public PooledObject<Fury> wrap(Fury fury) {
        return new DefaultPooledObject<>(fury);
    }

    @Override
    public void destroyObject(PooledObject<Fury> pooledObject) {
        // 清理资源（如果需要）
        return;
    }

    @Override
    public boolean validateObject(PooledObject<Fury> pooledObject) {
        // 验证对象是否有效
        return true;
    }
}
