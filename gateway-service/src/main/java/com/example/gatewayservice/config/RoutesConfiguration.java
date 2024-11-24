package com.example.gatewayservice.config;

import com.example.gatewayservice.filter.Resilience4jRateLimiterFilter;
import com.example.gatewayservice.filter.UserAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfiguration {

    @Autowired
    private UserAuthenticationFilter userAuthenticationFilter;

    @Autowired
    private Resilience4jRateLimiterFilter resilience4jRateLimiterFilter;

    @Bean
    public RouteLocator declare(RouteLocatorBuilder builder) {


        return builder.routes()
                // 不需要认证的user-service路由
                .route("user-service-public", r -> r.path("/users/register", "/users/login", "/users/refresh-token")
                        .filters(f -> f
                                .filter(resilience4jRateLimiterFilter.apply(new Resilience4jRateLimiterFilter.Config("userServiceRateLimiter")))
                        )
                        .uri("lb://user-service"))
                // 需要认证的user-service路由
                .route("user-service-protected", r -> r.path("/users/logout", "/users/unregister")
                        .filters(f -> f
                                .filter(userAuthenticationFilter.apply(new UserAuthenticationFilter.Config()))
                                .filter(resilience4jRateLimiterFilter.apply(new Resilience4jRateLimiterFilter.Config("userServiceRateLimiter")))
                        )
                        .uri("lb://user-service"))
                .route("product-service", r -> r.path("/products/**")
                        .filters(f -> f
                                        .filter(resilience4jRateLimiterFilter.apply(new Resilience4jRateLimiterFilter.Config("productServiceRateLimiter")))
                        )
                        .uri("lb://product-service"))
                .route("order-service", r -> r.path("/orders/**")
                        .filters(f -> f
                                .filter(userAuthenticationFilter.apply(new UserAuthenticationFilter.Config()))
                                .filter(resilience4jRateLimiterFilter.apply(new Resilience4jRateLimiterFilter.Config("orderServiceRateLimiter")))
                        )
                        .uri("lb://order-service"))
                .build();
    }
}
