package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.UserResponseDto;
import com.energyfactory.energy_factory.dto.UserUpdateRequestDto;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 프로필 관리 컨트롤러
 * 사용자 프로필 조회 및 수정 기능을 제공
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Profile", description = "사용자 프로필 관련 API")
public class UserProfileController {

    private final UserService userService;

    @Autowired
    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "사용자 프로필 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserResponseDto>> getProfile() {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/profile")
    @Operation(summary = "사용자 프로필 수정")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserResponseDto>> updateProfile(
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }
}