package com.example.orderservice.config.handler;

import com.example.common.response.ApiCode;
import com.example.common.response.ApiResponse;
import com.example.orderservice.exception.AccessDeniedException;
import com.example.orderservice.exception.OrderNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.ok(new ApiResponse<>(ApiCode.ACCESS_DENIED, "Access denied", null));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleOrderNotFoundException(OrderNotFoundException e) {
        log.warn("Order not found: {}", e.getMessage());
        return ResponseEntity.ok(new ApiResponse<>(ApiCode.ORDER_NOT_FOUND, "Order not found", null));
    }

    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleNumberFormatException(NumberFormatException e) {
        log.warn("Invalid number format: {}", e.getMessage());
        return ResponseEntity.ok(new ApiResponse<>(ApiCode.PARAM_ERROR, "Invalid parameter format", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.ok(new ApiResponse<>(ApiCode.SYS_ERROR, "Internal server error", null));
    }
}
