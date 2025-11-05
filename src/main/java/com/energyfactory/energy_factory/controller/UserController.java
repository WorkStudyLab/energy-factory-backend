package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.SignupRequestDto;
import com.energyfactory.energy_factory.dto.SignupResponseDto;
import com.energyfactory.energy_factory.dto.UserAdditionalInfoRequestDto;
import com.energyfactory.energy_factory.dto.UserResponseDto;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.energyfactory.energy_factory.dto.CustomUserDetails;
import jakarta.validation.Valid;

/**
 * 사용자 관리 컨트롤러
 * 회원가입, 사용자 정보 조회, 비밀번호 변경, 회원 탈퇴 기능을 제공
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "사용자 관리 관련 API")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, userService.signup(signupRequestDto)));
    }

    @GetMapping("/me")
    @Operation(
        summary = "내 정보 조회 (마이페이지)",
        description = "JWT 토큰으로 인증된 사용자의 정보를 조회합니다. 기본 배송지 주소가 자동으로 포함됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "사용자 정보 조회 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserResponseDto.class)
            )
        )
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, user));
    }

    @PutMapping("/password")
    @Operation(
        summary = "비밀번호 변경",
        description = "JWT 토큰으로 인증된 사용자의 비밀번호를 변경합니다. 현재 비밀번호 확인이 필요합니다."
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {
        Long userId = userDetails.getUser().getId();
        userService.changePassword(userId, currentPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @DeleteMapping("/me")
    @Operation(
        summary = "회원 탈퇴",
        description = "JWT 토큰으로 인증된 사용자의 계정을 삭제합니다. 이 작업은 되돌릴 수 없습니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @PutMapping("/additional-info")
    @Operation(
        summary = "소셜 로그인 후 추가 정보 업데이트",
        description = "네이버 소셜 로그인 후 부족한 정보(전화번호, 생년월일, 배송지)를 추가로 입력받습니다."
    )
    public ResponseEntity<ApiResponse<UserResponseDto>> updateAdditionalInfo(
            Authentication authentication,
            @Valid @RequestBody UserAdditionalInfoRequestDto requestDto) {
        String email = authentication.getName();
        UserResponseDto user = userService.updateAdditionalInfo(email, requestDto);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, user));
    }

    @GetMapping("/profile")
    @Operation(
        summary = "현재 로그인한 사용자 정보 조회",
        description = "JWT 토큰으로 인증된 사용자의 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, user));
    }

}