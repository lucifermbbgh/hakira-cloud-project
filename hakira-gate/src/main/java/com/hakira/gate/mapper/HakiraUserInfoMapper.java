package com.hakira.gate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hakira.gate.pojo.dao.HakiraUserInfo;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hakira-gate
 * @BelongsPackage: com.hakira.gate.mapper
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-26  23:11:05
 * @Description: Mybatis-Plus中的Mapper接口必须继承BaseMapper基类
 * @Version: 1.0
 */
@Mapper
public interface HakiraUserInfoMapper extends BaseMapper<HakiraUserInfo> {
    int createUserInfo(HakiraUserInfo HakiraUserInfo);

    int modifyUserInfo(HakiraUserInfo HakiraUserInfo);

    int deleteUserInfo(BigInteger id);

    HakiraUserInfo queryUserInfo(BigInteger id);

    List<HakiraUserInfo> queryAllUserInfo();

    List<HakiraUserInfo> queryUserInfoList(Map<String, Object> queryParams);
}
