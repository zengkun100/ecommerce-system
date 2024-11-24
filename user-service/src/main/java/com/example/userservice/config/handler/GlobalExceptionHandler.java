package com.example.userservice.config.handler;

import com.example.common.response.ApiCode;
import com.example.common.response.ApiResponse;
import com.example.userservice.exception.TokenExpiredException;
import com.example.userservice.exception.TokenNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TokenExpiredException.class)
public ResponseEntity<ApiResponse<?>> handleTokenExpiredException(TokenExpiredException e) {
    return ResponseEntity.ok(ApiResponse.error(ApiCode.TOKEN_EXPIRED, "token已过期"));
}

@ExceptionHandler(TokenNotFoundException.class)
public ResponseEntity<ApiResponse<?>> handleTokenNotFoundException(TokenNotFoundException e) {
    return ResponseEntity.ok(ApiResponse.error(ApiCode.TOKEN_INVALID, "token无效"));
}

//    @ExceptionHandler(TokenExpiredException.class)
//    public ResponseEntity<ApiResponse<Void>> handleTokenExpiredException(TokenExpiredException e) {
//        return ResponseEntity.ok(new ApiResponse<>(ApiCode.TOKEN_EXPIRED,
//                "token已过期", null));
//    }

//    @ExceptionHandler(TokenNotFoundException.class)
//    public ResponseEntity<ApiResponse<Void>> handleTokenNotFoundException(TokenNotFoundException e) {
//        return ResponseEntity.ok(new ApiResponse<>(ApiCode.TOKEN_NOT_FOUND,
//                e.getMessage(), null));
//    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.ok(new ApiResponse<>(ApiCode.PARAM_ERROR,
                e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity.ok(new ApiResponse<>(ApiCode.SYS_ERROR,
                "系统内部错误: " + e.getMessage(), null));
    }
}
