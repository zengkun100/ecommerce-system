package com.example.gatewayservice.filter;

import com.example.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

@Component
public class UserAuthenticationFilter extends AbstractGatewayFilterFactory<UserAuthenticationFilter.Config> {

    private final WebClient.Builder webClientBuilder;

    @Autowired
    public UserAuthenticationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String token = request.getHeaders().getFirst("Authorization");

            if (token == null || !token.startsWith("Bearer ")) {
                return onError(exchange, "未提供认证token", HttpStatus.UNAUTHORIZED);
            }

            return webClientBuilder.build()
                .get()
                .uri("lb://user-service/users/validate")
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                .flatMap(apiResponse -> {
                    if (apiResponse.getCode() != 0) {
                        return onError(exchange, apiResponse.getMessage(), HttpStatus.UNAUTHORIZED);
                    }
                    
                    String userId = apiResponse.getData();
                    if (userId == null || userId.isEmpty()) {
                        return onError(exchange, "无效的认证信息", HttpStatus.UNAUTHORIZED);
                    }

                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", userId)
                            .build();

                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .onErrorResume(e -> onError(exchange, "认证服务异常", HttpStatus.UNAUTHORIZED));
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format("{\"message\":\"%s\",\"code\":%d}", message, status.value());
        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // 配置属性
    }
}
