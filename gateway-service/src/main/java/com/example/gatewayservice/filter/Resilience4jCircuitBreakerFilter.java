package com.example.gatewayservice.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Resilience4jCircuitBreakerFilter extends AbstractGatewayFilterFactory<Resilience4jCircuitBreakerFilter.Config> {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Resilience4jCircuitBreakerFilter(CircuitBreakerRegistry circuitBreakerRegistry) {
        super(Config.class);
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public GatewayFilter apply(Config config) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(config.getCircuitBreakerName());

        return (exchange, chain) -> 
            Mono.fromCallable(() -> 
                circuitBreaker.executeSupplier(() -> chain.filter(exchange))
            ).flatMap(mono -> mono)
             .onErrorResume(throwable -> {
                exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return Mono.empty();
             });
    }

    public static class Config {
        private final String circuitBreakerName;

        public Config(String circuitBreakerName) {
            this.circuitBreakerName = circuitBreakerName;
        }

        public String getCircuitBreakerName() {
            return circuitBreakerName;
        }
    }
}
