package com.hakira.ledger.gateway.handler;

import com.alibaba.fastjson2.JSON;
import com.hakira.common.pojo.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 网关全局异常处理器
 * 将401/403及其他异常转换为统一的JSON响应格式
 *
 * @author hakiraKafka
 */
@Order(-1)
@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(
                MediaType.valueOf("application/json;charset=utf-8"));

        Result<?> result;
        HttpStatus httpStatus;

        if (ex instanceof ResponseStatusException rse) {
            httpStatus = HttpStatus.valueOf(rse.getStatusCode().value());
            if (httpStatus == HttpStatus.UNAUTHORIZED) {
                result = Result.returnFail("401", "未授权访问：请先登录");
            } else if (httpStatus == HttpStatus.FORBIDDEN) {
                result = Result.returnFail("403", "禁止访问：权限不足");
            } else {
                result = Result.returnFail(String.valueOf(httpStatus.value()), rse.getReason());
            }
        } else if (ex instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
            result = Result.returnFail("404", "服务未找到");
        } else {
            log.error("网关异常: ", ex);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            result = Result.returnUnknown("500", "网关内部错误: " + ex.getMessage());
        }

        response.setStatusCode(httpStatus);

        byte[] bytes = JSON.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
