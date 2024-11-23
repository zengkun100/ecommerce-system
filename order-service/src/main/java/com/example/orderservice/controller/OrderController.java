package com.example.orderservice.controller;

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
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        try {
            String orderId = orderService.placeOrder(orderRequest, Long.parseLong(userId));
            return ResponseEntity.ok("Order placed successfully. Order ID: " + orderId);
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Failed to place order: " + e.getMessage());
        }
    }
}
