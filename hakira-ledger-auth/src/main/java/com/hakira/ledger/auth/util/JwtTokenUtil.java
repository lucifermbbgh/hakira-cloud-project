package com.hakira.ledger.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT令牌工具类
 * 负责生成和验证JWT令牌
 *
 * @author hakiraKafka
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 为用户生成JWT令牌
     *
     * @param username     用户名
     * @param authorities  用户权限集合
     * @return JWT令牌字符串
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        // 提取角色名称列表
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration);

        String token = JWT.create()
                .withSubject(username)
                .withClaim("roles", roles)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(algorithm);

        log.debug("为用户 {} 生成JWT令牌, 角色: {}, 过期时间: {}", username, roles, expiresAt);
        return token;
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌字符串
     * @return true-有效, false-无效
     */
    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.warn("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从JWT令牌中提取用户名
     *
     * @param token JWT令牌字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject();
        } catch (JWTVerificationException e) {
            log.warn("从JWT令牌提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从JWT令牌中提取角色列表
     *
     * @param token JWT令牌字符串
     * @return 角色字符串数组
     */
    public String[] getRolesFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("roles").asArray(String.class);
        } catch (JWTVerificationException e) {
            log.warn("从JWT令牌提取角色失败: {}", e.getMessage());
            return new String[0];
        }
    }
}
