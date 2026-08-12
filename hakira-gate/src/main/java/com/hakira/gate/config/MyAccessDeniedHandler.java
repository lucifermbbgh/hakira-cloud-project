package com.hakira.gate.config;

import com.alibaba.fastjson2.JSON;
import com.hakira.common.constants.HttpStatusEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: hakira-gate
 * @BelongsPackage: com.hakira.gate.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-02  11:58:59
 * @Description: 自定义权限拒绝处理器
 * @Version: 1.0
 */
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        Map result = new HashMap();
        result.put("code", HttpStatusEnum.FORBIDDEN.getCode());// 响应状态码403拒绝未授权的访问
        result.put("message", "没有权限");// 响应信息

        // 请求结果转换为json字符串
        String jsonResult = JSON.toJSONString(result);

        // 响应json数据
        response.setContentType("application/json;charset=utf-8");// 设置响应头
        response.getWriter().println(jsonResult);// 响应json数据
    }
}
