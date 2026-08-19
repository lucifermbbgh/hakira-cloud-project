package com.hakira.ledger.auth.controller;

import com.hakira.common.pojo.common.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: hakira-ledger-auth
 * @BelongsPackage: com.hakira.ledger.auth
 * @Author: hakiraKafka
 * @CreateTime: 2024-01-31  11:42:27
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
public class HakiraGateController {
    /**
     * @description: 主页-用户认证信息
     * @author: hakiraKafka
     * @date: 2024/2/1 23:32
     * @return: com.hakira.common.pojo.common.Result<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @GetMapping("/")
    public Result<Map<String, Object>> openGate() {
        // 获取当前登录用户信息上下文
        SecurityContext context = SecurityContextHolder.getContext();
        // 获取当前登录用户信息
        Authentication authentication = context.getAuthentication();
        // 获取当前登录用户身份信息
        Object principal = authentication.getPrincipal();
        // 获取当前登录用户凭证信息
        Object credentials = authentication.getCredentials();
        // 获取当前登录用户权限/角色信息
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        Map<String, Object> retMap = new HashMap<>();
        retMap.put("username", authentication.getName());
        retMap.put("authorities", authorities);
        retMap.put("principal", principal);
        retMap.put("credentials", credentials);

        return Result.returnSuccess(retMap);
    }
}
