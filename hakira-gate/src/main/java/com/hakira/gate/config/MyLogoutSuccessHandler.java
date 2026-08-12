package com.hakira.gate.config;

import com.alibaba.fastjson2.JSON;
import com.hakira.common.constants.HttpStatusEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: hakira-gate
 * @BelongsPackage: com.hakira.gate.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-01  22:12:20
 * @Description: 登出成功处理类
 * @Version: 1.0
 */
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Map result = new HashMap();
        result.put("code", HttpStatusEnum.OK.getCode());// 响应状态码200表示成功
        result.put("message", "登出成功");// 响应信息

        // 请求结果转换为json字符串
        String jsonResult = JSON.toJSONString(result);

        // 响应json数据
        response.setContentType("application/json;charset=utf-8");// 设置响应头
        response.getWriter().println(jsonResult);// 响应json数据
    }
}
