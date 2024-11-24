package com.example.orderservice.config.handler;

import com.example.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理参数校验异常（@Validated）
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("参数校验失败: {}", e.getMessage());
        return ResponseEntity.ok(new ApiResponse<>(400, e.getMessage(), null));
    }

    // 处理其他未预期的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);
        return ResponseEntity.ok(new ApiResponse<>(500, "系统内部错误", null));
    }

}
