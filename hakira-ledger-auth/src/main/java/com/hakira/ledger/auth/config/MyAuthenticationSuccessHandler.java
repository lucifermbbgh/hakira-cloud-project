package com.hakira.ledger.auth.config;

import com.alibaba.fastjson2.JSON;
import com.hakira.common.constants.HttpStatusEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: hakira-ledger-auth
 * @BelongsPackage: com.hakira.ledger.auth.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-01  21:46:41
 * @Description: 登录成功处理类
 * @Version: 1.0
 */
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    /**
     * @description: 登录成功处理
     * @description: 对返回的结果集进行JSON转换，返回给前端页面做进一步处理
     * @description: 添加响应头
     * @author: hakiraKafka
     * @date: 2024/2/1 22:00
     * @param: request
     * @param: response
     * @param: authentication
     **/
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object principal = authentication.getPrincipal();// 获取用户身份信息
//        Object credentials = authentication.getCredentials();// 获取用户密码（凭证信息）
//        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();// 获取用户权限信息

        Map result = new HashMap();
        result.put("code", HttpStatusEnum.OK.getCode());// 响应状态码200表示成功
        result.put("message", "登录成功");// 响应信息
        result.put("data", principal);// 响应数据

        // 请求结果转换为json字符串
        String jsonResult = JSON.toJSONString(result);

        // 响应json数据
        response.setContentType("application/json;charset=utf-8");// 设置响应头
        response.getWriter().println(jsonResult);// 响应json数据
    }
}
