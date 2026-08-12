package com.hakira.ledger.entry.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.mapper
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-17  22:37:45
 * @Description: TODO
 * @Version: 1.0
 */
@Mapper
public interface HakiraMarketOrderMapper {

    int createOrderInfo();
}
