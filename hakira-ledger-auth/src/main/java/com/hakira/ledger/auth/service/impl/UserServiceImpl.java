package com.hakira.ledger.auth.service.impl;

import com.hakira.common.exception.ServiceFailException;
import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.auth.manager.DBUserManager;
import com.hakira.ledger.auth.manager.PasswordEncodeManager;
import com.hakira.ledger.auth.mapper.HakiraUserInfoMapper;
import com.hakira.ledger.auth.mapper.HakiraUserMapper;
import com.hakira.ledger.auth.pojo.dao.HakiraUser;
import com.hakira.ledger.auth.pojo.dao.HakiraUserInfo;
import com.hakira.ledger.auth.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.auth.api.order.impl
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-29  22:25:38
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements IUserService {
    @Autowired
    private HakiraUserMapper HakiraUserMapper;
    @Autowired
    private HakiraUserInfoMapper HakiraUserInfoMapper;

    @Resource
    private PasswordEncodeManager passwordEncodeManager;
    @Resource
    private DBUserManager dbUserManager;

    @Override
    public Result<HakiraUser> register(HakiraUser HakiraUser) {
        String username = HakiraUser.getUsername();
        int checkName = HakiraUserMapper.queryUserByUsername(username);
        if (checkName > 0) {
            return Result.returnFail("10001002", "注册失败，用户已存在！");
        }

        int user = HakiraUserMapper.createUser(HakiraUser);
        if (user > 0) {
            return Result.returnSuccess(HakiraUser);
        } else {
            return Result.returnFail("10001002", "注册失败，用户已存在！");
        }
    }

    @Override
    public Result<HakiraUser> resetPassword(String username, String password) {
        return null;
    }

    @Override
    public Result deleteAccount(HakiraUser HakiraUser) {
        return null;
    }

    @Override
    public Result<List<HakiraUser>> queryUserList(Map<String, Object> queryParams) throws ServiceFailException {
        try {
            List<HakiraUser> HakiraUsers = HakiraUserMapper.queryUserList(queryParams);
            return Result.returnSuccess(HakiraUsers);
        } catch (Exception e) {
            log.error("查询用户列表失败！报错信息：{}", ExceptionUtils.getStackTrace(e));
            throw new ServiceFailException(
                    ServiceFailException.DEFAULT_ERROR_CODE,
                    ServiceFailException.DEFAULT_ERROR_INFO);
        }
    }

    @Override
    public Result<HakiraUserInfo> queryUserInfo(String username, String password) {
        return null;
    }

    @Override
    public Result<List<HakiraUserInfo>> queryUserInfoList(Map<String, Object> queryParams) {
        return null;
    }

    @Override
    public Result<HakiraUserInfo> updateUserInfo(HakiraUserInfo HakiraUserInfo) {
        return null;
    }

    @Override
    public Result<HakiraUserInfo> addUserInfo(HakiraUserInfo HakiraUserInfo) {
        return null;
    }

    @Override
    public Result<HakiraUser> registerWithUserDetails(HakiraUser HakiraUser) {
        try {
            Collection<GrantedAuthority> HakiraUserAuthorities = new ArrayList<>();
            User user = (User) User.withDefaultPasswordEncoder()
                    .username(HakiraUser.getUsername())
                    .password(HakiraUser.getPassword())
                    .authorities(HakiraUserAuthorities)
                    .build();
            dbUserManager.createUser(user);
            return Result.returnSuccess(HakiraUser);
        } catch (Exception e) {
            log.error("注册失败！报错信息：{}", ExceptionUtils.getStackTrace(e));
            return Result.returnFail("10001002", "注册失败，用户已存在！");
        }
    }
}
