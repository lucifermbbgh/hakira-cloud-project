package com.hakira.ledger.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT认证网关过滤器工厂
 * 对需要认证的路由进行JWT token验证，验证通过后将用户信息注入请求头传递给下游服务
 *
 * @author hakiraKafka
 */
@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGatewayFilterFactory.class);

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public String name() {
        return "JwtAuth";
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // 公共路径跳过JWT验证
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // 获取Authorization头
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("请求缺少Authorization头或格式不正确: {}", path);
                return unauthorizedResponse(exchange, "缺少认证令牌");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            try {
                // 验证JWT token
                Algorithm algorithm = Algorithm.HMAC256(secret);
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT decodedJWT = verifier.verify(token);

                // 提取用户信息
                String username = decodedJWT.getSubject();
                List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

                log.debug("JWT验证成功, 用户: {}, 角色: {}", username, roles);

                // 将用户信息注入请求头，传递给下游服务
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Name", username)
                        .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                        .header("X-User-Id", decodedJWT.getClaim("userId") != null
                                ? decodedJWT.getClaim("userId").asString() : "")
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JWTVerificationException e) {
                log.warn("JWT验证失败: {}", e.getMessage());
                return unauthorizedResponse(exchange, "令牌无效或已过期");
            }
        };
    }

    /**
     * 判断是否为公共路径（无需认证）
     */
    private boolean isPublicPath(String path) {
        return path.equals("/login") || path.startsWith("/login/")
                || path.startsWith("/oauth2/") || path.equals("/logout");
    }

    /**
     * 返回401未授权JSON响应
     */
    private Mono<Void> unauthorizedResponse(
            org.springframework.web.server.ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(
                MediaType.valueOf("application/json;charset=utf-8"));

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);

        String jsonResult = JSON.toJSONString(result);
        byte[] bytes = jsonResult.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
