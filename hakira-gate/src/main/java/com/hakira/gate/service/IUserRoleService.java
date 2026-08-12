package com.hakira.gate.service;


import com.hakira.gate.pojo.dao.HakiraRole;

import java.util.List;

/**
 * @BelongsProject: hakira-market-parent
 * @BelongsPackage: com.hakira.gate.api.user
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-25  23:31:27
 * @Description: TODO
 * @Version: 1.0
 */
public interface IUserRoleService {
    /**
     * @description: 添加用户角色
     * @author: hakiraKafka
     * @date: 2024/1/26 23:16
     * @param: userId
     * @param: roleIds
     * @return: int
     **/
    int authUserRoles(String userId, String[] roleIds);

    /**
     * @description: 删除用户角色
     * @author: hakiraKafka
     * @date: 2024/1/26 23:16
     * @param: userId
     * @param: roleIds
     * @return: int
     **/
    int unAuthUserRoles(String userId, String[] roleIds);

    /**
     * @description: 查询用户角色
     * @author: hakiraKafka
     * @date: 2024/1/26 23:19
     * @param: userId
     * @return: java.util.List<com.hakira.common.pojo.dao.hakiraRole>
     **/
    List<HakiraRole> queryUserRoles(String userId);
}
