package com.example.orderservice.controller;

import com.example.common.response.ApiResponse;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/orders")
@Slf4j
public class OrderController {
    //    @Autowired
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    @CircuitBreaker(name = "productServiceCircuitBreaker", fallbackMethod = "placeOrderFallback")
    public ResponseEntity<ApiResponse<String>> placeOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            return ResponseEntity.ok(new ApiResponse<>(401, "User not authenticated", null));
        }

        log.info("Received order request from user: {}, orderRequest: {}", userId, orderRequest);

        String orderId = orderService.placeOrder(orderRequest, Long.parseLong(userId));
        return ResponseEntity.ok(new ApiResponse<>(0, "Order placed successfully", orderId));
    }

    // 添加 fallback 方法
    public ResponseEntity<ApiResponse<String>> placeOrderFallback(OrderRequest orderRequest, HttpServletRequest request, Exception e) {
        return ResponseEntity.ok(new ApiResponse<>(503, "Product service is not available", null));
    }
}
