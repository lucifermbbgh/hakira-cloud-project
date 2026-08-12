package com.hakira.gate.config;

import com.alibaba.fastjson2.JSON;
import com.hakira.common.constants.HttpStatusEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: hakira-gate
 * @BelongsPackage: com.hakira.gate.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-01  22:25:57
 * @Description: 未经认证的请求拒绝后返回信息处理
 * @Version: 1.0
 */
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 获取异常信息
//        String localizedMessage = authException.getLocalizedMessage();
        String localizedMessage = "用户未登录！";

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
