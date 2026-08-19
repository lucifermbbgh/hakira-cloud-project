package com.hakira.ledger.auth.config;

import com.alibaba.fastjson2.JSON;
import com.hakira.common.constants.HttpStatusEnum;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: hakira-ledger-auth
 * @BelongsPackage: com.hakira.ledger.auth.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-01  23:41:15
 * @Description: 自定义Session会话并发处理
 * @Version: 1.0
 */
public class MySessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {
    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        Map result = new HashMap();
        result.put("code", HttpStatusEnum.OK.getCode());// 响应状态码200表示成功
        result.put("message", "登出成功");// 响应信息

        // 请求结果转换为json字符串
        String jsonResult = JSON.toJSONString(result);

        // 响应json数据
        HttpServletResponse response = event.getResponse();
        response.setContentType("application/json;charset=utf-8");// 设置响应头
        response.getWriter().println(jsonResult);// 响应json数据
    }
}
