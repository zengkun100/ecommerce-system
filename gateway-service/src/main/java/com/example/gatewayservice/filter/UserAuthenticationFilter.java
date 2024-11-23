package com.example.gatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.io.buffer.DataBuffer;

@Component
public class UserAuthenticationFilter extends AbstractGatewayFilterFactory<UserAuthenticationFilter.Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String token = request.getHeaders().getFirst("Authorization");

            // 检查token是否存在
            if (token == null || !token.startsWith("Bearer ")) {
                return onError(exchange, "未提供认证token", HttpStatus.UNAUTHORIZED);
            }

            try {
                token = token.substring(7);
                Claims claims = Jwts.parser()
                        .setSigningKey("your_secret_key")
                        .parseClaimsJws(token)
                        .getBody();
                String userId = claims.get("userId", String.class);

                // 将用户信息传递给下游服务
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (ExpiredJwtException e) {
                return onError(exchange, "token已过期", HttpStatus.UNAUTHORIZED);
            } catch (JwtException e) {
                return onError(exchange, "无效的token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format("{\"error\":\"%s\",\"status\":%d}", message, status.value());
        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // 配置属性
    }
}
