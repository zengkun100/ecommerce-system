package com.example.gatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class UserAuthenticationFilter extends AbstractGatewayFilterFactory<UserAuthenticationFilter.Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Claims claims = Jwts.parser()
                        .setSigningKey("your_secret_key")
                        .parseClaimsJws(token)
                        .getBody();
                String userId = claims.get("userId", String.class);

                ServerHttpRequest request = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .build();

                return chain.filter(exchange.mutate().request(request).build());
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // 配置属性
    }
}
