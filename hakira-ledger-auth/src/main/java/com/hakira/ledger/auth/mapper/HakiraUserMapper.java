package com.hakira.ledger.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hakira.ledger.auth.pojo.dao.HakiraUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.auth.mapper
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-17  22:37:45
 * @Description: Mybatis-Plus中的Mapper接口必须继承BaseMapper基类
 * @Version: 1.0
 */
@Mapper
public interface HakiraUserMapper extends BaseMapper<HakiraUser> {
    int createUser(HakiraUser HakiraUser);

    int modifyPassword(BigInteger id, String password);

    int deleteUser(BigInteger id);

    HakiraUser queryUserById(BigInteger id);

    @Select("select count(username) " +
            "from hakira_user " +
            "where username = #{username}"
    )
    int queryUserByUsername(String username);

    List<HakiraUser> queryUserList(Map<String, Object> queryParams);
}
