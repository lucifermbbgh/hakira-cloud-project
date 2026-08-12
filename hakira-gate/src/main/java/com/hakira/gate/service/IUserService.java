package com.hakira.gate.service;

import com.hakira.common.pojo.common.Result;
import com.hakira.gate.pojo.dao.HakiraUser;
import com.hakira.gate.pojo.dao.HakiraUserInfo;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.service.user
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-17  22:10:40
 * @Description: TODO
 * @Version: 1.0
 */
public interface IUserService {
    /**
     * @description: 用户注册
     * @author: hakiraKafka
     * @date: 2024/1/25 23:11
     * @param: username
     * @param: password
     * @return: com.hakira.common.pojo.common.Result<com.hakira.common.pojo.dao.hakiraUser>
     **/
    Result<HakiraUser> register(HakiraUser hakiraUser);

    /**
     * @description: 重置密码
     * @author: hakiraKafka
     * @date: 2024/1/25 23:12
     * @param: username
     * @param: password
     * @return: com.hakira.common.pojo.common.Result<com.hakira.common.pojo.dao.hakiraUser>
     **/
    Result<HakiraUser> resetPassword(String username, String password);

    /**
     * @description: 用户注销
     * @author: hakiraKafka
     * @date: 2024/1/25 23:12
     * @param: hakiraUser
     * @return: com.hakira.common.pojo.common.Result
     **/
    Result deleteAccount(HakiraUser hakiraUser);

    /**
     * @description: 查询用户列表
     * @author: hakiraKafka
     * @date: 2024/2/1 11:55
     * @return: com.hakira.common.pojo.common.Result<java.util.List<com.hakira.gate.pojo.dao.hakiraUser>>
     **/
    Result<List<HakiraUser>> queryUserList(Map<String, Object> queryParams) throws Exception;

    /**
     * @description: 查询用户信息
     * @author: hakiraKafka
     * @date: 2024/1/25 23:12
     * @param: username
     * @param: password
     * @return: com.hakira.common.pojo.common.Result<com.hakira.common.pojo.dao.hakiraUserInfo>
     **/
    Result<HakiraUserInfo> queryUserInfo(String username, String password);

    /**
     * @description: 查询用户信息列表
     * @author: hakiraKafka
     * @date: 2024/2/1 11:55
     * @return: com.hakira.common.pojo.common.Result<java.util.List<com.hakira.gate.pojo.dao.hakiraUserInfo>>
     **/
    Result<List<HakiraUserInfo>> queryUserInfoList(Map<String, Object> queryParams);

    /**
     * @description: 修改用户信息
     * @author: hakiraKafka
     * @date: 2024/1/25 23:12
     * @param: hakiraUserInfo
     * @return: com.hakira.common.pojo.common.Result<com.hakira.common.pojo.dao.hakiraUserInfo>
     **/
    Result<HakiraUserInfo> updateUserInfo(HakiraUserInfo hakiraUserInfo);

    /**
     * @description: 修改用户信息
     * @author: hakiraKafka
     * @date: 2024/1/25 23:12
     * @param: hakiraUserInfo
     * @return: com.hakira.common.pojo.common.Result<com.hakira.common.pojo.dao.hakiraUserInfo>
     **/
    Result<HakiraUserInfo> addUserInfo(HakiraUserInfo hakiraUserInfo);

    Result<HakiraUser> registerWithUserDetails(HakiraUser hakiraUser);
}
