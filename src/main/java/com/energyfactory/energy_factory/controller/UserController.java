package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.SignupRequestDto;
import com.energyfactory.energy_factory.dto.SignupResponseDto;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User Authentication", description = "사용자 인증 관련 API")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, userService.signup(signupRequestDto)));
    }

}