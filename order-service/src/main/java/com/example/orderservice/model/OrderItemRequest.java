package com.example.orderservice.model;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
    
    // getters and setters
    public Long getProductId() {
        return productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
}
