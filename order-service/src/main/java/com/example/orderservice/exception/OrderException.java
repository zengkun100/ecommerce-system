package com.example.orderservice.exception;

public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }
}