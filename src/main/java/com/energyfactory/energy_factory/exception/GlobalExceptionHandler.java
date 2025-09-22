package com.energyfactory.energy_factory.exception;

import com.energyfactory.energy_factory.controller.UserController;
import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> businessException(BusinessException e) {
        return ResponseEntity
                .status(e.getResultCode().getStatus())
                .body(ApiResponse.of(e.getResultCode(), null));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> authException(AuthException e) {
        return ResponseEntity
                .status(e.getResultCode().getStatus())
                .body(ApiResponse.of(e.getResultCode(), null));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> jwtException(JwtException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.of(ResultCode.INVALID_REFRESH_TOKEN, null));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ResultCode.INTERNAL_SERVER_ERROR, null));
    }
}