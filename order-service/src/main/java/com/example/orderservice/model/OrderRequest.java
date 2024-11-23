package com.example.orderservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
// @Setter
// @Getter
public class OrderRequest {
    private List<OrderItemRequest> orderItems;
    
    // getter and setter
    public List<OrderItemRequest> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItemRequest> orderItems) {
        this.orderItems = orderItems;
    }
}
