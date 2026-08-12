package com.hakira.ledger.auth.controller;

import com.alibaba.fastjson.JSON;
import com.hakira.common.exception.ServiceFailException;
import com.hakira.common.exception.ServiceUnknownException;
import com.hakira.common.pojo.common.Result;
import com.hakira.ledger.auth.pojo.dao.HakiraUser;
import com.hakira.ledger.auth.pojo.dao.HakiraUserInfo;
import com.hakira.ledger.auth.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: hakira
 * @BelongsPackage: com.hakira.ledger.auth.order.controller
 * @Author: hakiraKafka
 * @CreateTime: 2023-11-25  22:57:19
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private IUserService iUserService;
//    @Autowired
//    private RestTemplate restTemplate;

    @GetMapping("/getList")
//    @PreAuthorize("hasRole('USER')")
    @PreAuthorize("hasAnyAuthority('USER_SELECT')")
    public Result<List<HakiraUser>> getList(String queryParams) {
        try {
            Map<String, Object> inputParams = new HashMap<>();
            if (StringUtils.isNotBlank(queryParams)) {
                inputParams = JSON.parseObject(queryParams, Map.class);
            }
            return iUserService.queryUserList(inputParams);
        } catch (Exception e) {
            return Result.returnUnknown("99999999", ExceptionUtils.getStackTrace(e), null);
        }
    }

    /**
     * @description: 用户注册
     * @description: @PreAuthorize注解表示在方法调用之前，先进行用户的角色权限校验
     * @description: hasRole('ADMIN')表示当前用户角色必须是‘ADMIN’才能调用该方法
     * @description: authentication.name=='admin'表示当前用户账号必须是‘admin’才能调用该方法
     * @description: hasAnyAuthority('USER_SELECT')表示当前用户必须有赋权'USER_SELECT'权限才可以调用该方法
     * @author: hakiraKafka
     * @date: 2024/2/3 0:08
     * @param: HakiraUser
     * @return: com.hakira.common.pojo.common.Result
     **/
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN') and authentication.name == 'admin'")
    public Result register(@RequestBody HakiraUser HakiraUser) {
        try {
            return iUserService.registerWithUserDetails(HakiraUser);
        } catch (Exception e) {
            return Result.returnUnknown("99999999", ExceptionUtils.getStackTrace(e));
        }
    }

    @PostMapping("/registerByForm")
    @Transactional
    public Result registerByForm(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email) {
        try {
            HakiraUser HakiraUser = new HakiraUser().builder()
                    .username(username)
                    .password(password)
                    .build();
            HakiraUserInfo HakiraUserInfo = new HakiraUserInfo().builder()
                    .email(email)
                    .build();

            Result<HakiraUser> registerResult = iUserService.register(HakiraUser);
            if (registerResult.getResultCode().intValue() == Result.FAIL.intValue()) {
                throw new ServiceFailException(registerResult.getErrorCode(), registerResult.getErrorInfo());
            }
            if (registerResult.getResultCode().intValue() == Result.UNKNOWN.intValue()) {
                throw new ServiceUnknownException(registerResult.getErrorCode(), registerResult.getErrorInfo());
            }

            Result<HakiraUserInfo> addUserInfoResult = iUserService.addUserInfo(HakiraUserInfo);
            if (addUserInfoResult.getResultCode().intValue() == Result.FAIL.intValue()) {
                throw new ServiceFailException(registerResult.getErrorCode(), registerResult.getErrorInfo());
            }
            if (addUserInfoResult.getResultCode().intValue() == Result.UNKNOWN.intValue()) {
                throw new ServiceUnknownException(registerResult.getErrorCode(), registerResult.getErrorInfo());
            }

            return registerResult;
        } catch (ServiceFailException e) {
            return Result.returnFail(e.getErrorCode(), e.getErrorInfo());
        } catch (ServiceUnknownException e) {
            return Result.returnUnknown(e.getErrorCode(), e.getErrorInfo());
        } catch (Exception e) {
            return Result.returnUnknown("99999999", ExceptionUtils.getStackTrace(e));
        }
    }
}
