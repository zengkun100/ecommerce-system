package com.example.orderservice.controller;

import com.example.common.response.ApiResponse;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/orders")

public class OrderController {
//    @Autowired
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/place")
    public ResponseEntity<ApiResponse<String>> placeOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            return ResponseEntity.ok(new ApiResponse<>(401, "User not authenticated", null));
        }

        try {
            String orderId = orderService.placeOrder(orderRequest, Long.parseLong(userId));
            return ResponseEntity.ok(new ApiResponse<>(0, "Order placed successfully", orderId));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>(400, "Failed to place order: " + e.getMessage(), null));
        }
    }
}
