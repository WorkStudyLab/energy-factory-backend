package com.energyfactory.energy_factory.exception;

import com.energyfactory.energy_factory.controller.UserController;
import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = {RestController.class}, basePackageClasses = {UserController.class})

public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> businessException(BusinessException e) {
        return ResponseEntity
                .status(e.getResultCode().getStatus())
                .body(ApiResponse.of(e.getResultCode(), null));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.of(ResultCode.INTERNAL_SERVER_ERROR, null));
    }
}