package com.example.gatewayservice;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.gatewayservice.filter.UserAuthenticationFilter;
import com.example.gatewayservice.filter.Resilience4jRateLimiterFilter;

@Configuration
public class RoutesConfiguration {

    @Bean
    public RouteLocator declare(RouteLocatorBuilder builder) {

        return builder.routes()
                // 不需要认证的user-service路由
                .route("user-service-public", r -> r.path("/users/register", "/users/login", "/users/refresh-token")
                        .filters(f -> f
                                .filter(new Resilience4jRateLimiterFilter("userServiceRateLimiter")
                                        .apply(new Resilience4jRateLimiterFilter.Config()))
                        )
                        .uri("lb://user-service"))
                // 需要认证的user-service路由
                .route("user-service-protected", r -> r.path("/users/logout", "/users/unregister")
                        .filters(f -> f
                                .filter(new UserAuthenticationFilter().apply(new UserAuthenticationFilter.Config()))
                                .filter(new Resilience4jRateLimiterFilter("userServiceRateLimiter")
                                        .apply(new Resilience4jRateLimiterFilter.Config()))
                        )
                        .uri("lb://user-service"))
                .route("product-service", r -> r.path("/products/**")
                        .filters(f -> f
//                                .filter(new UserAuthenticationFilter().apply(new UserAuthenticationFilter.Config()))
                                        .filter(new Resilience4jRateLimiterFilter("productServiceRateLimiter")
                                                .apply(new Resilience4jRateLimiterFilter.Config()))
                        )
                        .uri("lb://product-service"))
                .route("order-service", r -> r.path("/orders/**")
                        .filters(f -> f
                                .filter(new UserAuthenticationFilter().apply(new UserAuthenticationFilter.Config()))
                                .filter(new Resilience4jRateLimiterFilter("orderServiceRateLimiter")
                                        .apply(new Resilience4jRateLimiterFilter.Config()))
                        )
                        .uri("lb://order-service"))
                // .route("user-service", r -> r.path("/users/**")
                //         .filters(f -> f
                //                 .filter(new Resilience4jRateLimiterFilter("userServiceRateLimiter")
                //                         .apply(new Resilience4jRateLimiterFilter.Config()))
                //         )
                //         .uri("lb://user-service"))
                .build();


//        return builder.routes()
//                .route("product-service", r -> r.path("/products/**")
//                        .uri("lb://product-service"))
//                .route("order-service", r -> r.path("/orders/**")
//                        .uri("lb://order-service"))
//                .build();
    }

//    @Bean
//    public GlobalFilter customFilter() {
//        return (exchange, chain) -> {
//            String accessToken = exchange.getRequest().getHeaders().getFirst("Authorization");
//            if (accessToken == null || !validateAccessToken(accessToken)) {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//            }
//            return chain.filter(exchange);
//        };
//    }
}
