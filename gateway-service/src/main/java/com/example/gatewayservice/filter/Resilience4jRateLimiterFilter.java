package com.example.gatewayservice.filter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;

//@Component
public class Resilience4jRateLimiterFilter implements GatewayFilterFactory<Resilience4jRateLimiterFilter.Config> {

    private final String rateLimiterName;

    public Resilience4jRateLimiterFilter(String rateLimiterName) {
        this.rateLimiterName = rateLimiterName;
    }

    public static class Config {
        // 配置属性，可以根据需要进行扩展
    }

    @Override
    public GatewayFilter apply(Config config) {
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.ofDefaults();
        RateLimiter rateLimiter = RateLimiter.of(rateLimiterName, rateLimiterConfig);

        return (exchange, chain) -> {
            if (rateLimiter.acquirePermission()) {
                return chain.filter(exchange);
            } else {
                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
                return exchange.getResponse().setComplete();
            }
        };
    }

}
