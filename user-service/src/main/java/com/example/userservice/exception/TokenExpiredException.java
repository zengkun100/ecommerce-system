package com.example.userservice.exception;

public class TokenExpiredException extends RuntimeException{
    public TokenExpiredException(String message) {
        super(message);
    }
}
