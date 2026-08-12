package com.hakira.ledger.auth.config;

import com.alibaba.fastjson2.JSON;
import com.hakira.ledger.auth.util.JwtTokenUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @BelongsProject: hakira-ledger-auth
 * @BelongsPackage: com.hakira.ledger.auth.config
 * @Author: hakiraKafka
 * @CreateTime: 2024-02-01  21:46:41
 * @Description: 登录成功处理类 — 生成JWT令牌并返回给客户端
 * @Version: 2.0
 */
@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(MyAuthenticationSuccessHandler.class);

    private final JwtTokenUtil jwtTokenUtil;

    public MyAuthenticationSuccessHandler(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * @description: 登录成功处理 — 生成JWT令牌并返回JSON响应
     * @description: 客户端收到token后应在后续请求中通过 Authorization: Bearer <token> 传递
     * @author: hakiraKafka
     * @date: 2024/2/1 22:00
     * @param: request
     * @param: response
     * @param: authentication
     **/
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 生成JWT令牌
        String token = jwtTokenUtil.generateToken(username, authorities);

        // 提取角色列表
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("用户 {} 登录成功, 角色: {}", username, roles);

        // 构造响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", username);
        data.put("roles", roles);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "登录成功");
        result.put("data", data);

        // 响应json数据
        String jsonResult = JSON.toJSONString(result);
        response.setContentType("application/json;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(jsonResult);
    }
}
