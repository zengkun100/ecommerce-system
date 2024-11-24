package com.example.gatewayservice.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
//import io.github.resilience4j.ratelimiter.exception.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class Resilience4jRateLimiterFilter extends AbstractGatewayFilterFactory<Resilience4jRateLimiterFilter.Config> {

    private final RateLimiterRegistry rateLimiterRegistry;

    public Resilience4jRateLimiterFilter(RateLimiterRegistry rateLimiterRegistry) {
        super(Config.class);
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    public static class Config {
        // 配置属性，可以根据需要进行扩展

        public Config(String rateLimiterName) {
            this.rateLimiterName = rateLimiterName;
        }

        private final String rateLimiterName;

        public String getRateLimiterName() {
            return rateLimiterName;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        // 直接从注册表获取已配置的 RateLimiter
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(config.getRateLimiterName());

        return (exchange, chain) -> {
            return Mono.defer(() -> {

                boolean permission = rateLimiter.acquirePermission();
                if (permission) {
                    return chain.filter(exchange);
                } else {
                    log.warn("Rate limit exceeded");
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return Mono.empty();
                }
            });
        };
    }

}
