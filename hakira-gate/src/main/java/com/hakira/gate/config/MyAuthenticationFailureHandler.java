package com.hakira.gate.config;

import com.alibaba.fastjson2.JSON;
import com.hakira.common.constants.HttpStatusEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: hakira-gate
 * @BelongsPackage: com.hakira.gate.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-01  22:00:20
 * @Description: 登录失败处理类
 * @Version: 1.0
 */
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {
    /**
     * @description: 登录失败处理
     * @description: 对返回的结果集进行JSON转换，返回给前端页面做进一步处理
     * @description: 添加响应头
     * @author: hakiraKafka
     * @date: 2024/2/1 22:07
     * @param: request
     * @param: response
     * @param: exception
     **/
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 获取异常信息
        String localizedMessage = exception.getLocalizedMessage();

        Map result = new HashMap();
        result.put("code", HttpStatusEnum.FORBIDDEN.getCode());// 响应状态码403表示登录失败禁止访问
        result.put("message", localizedMessage);// 响应信息

        // 请求结果转换为json字符串
        String jsonResult = JSON.toJSONString(result);

        // 响应json数据
        response.setContentType("application/json;charset=utf-8");// 设置响应头
        response.getWriter().println(jsonResult);// 响应json数据
    }
}
