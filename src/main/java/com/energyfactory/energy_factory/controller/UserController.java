package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.SignupRequestDto;
import com.energyfactory.energy_factory.dto.SignupResponseDto;
import com.energyfactory.energy_factory.dto.UserAdditionalInfoRequestDto;
import com.energyfactory.energy_factory.dto.UserResponseDto;
import com.energyfactory.energy_factory.jwt.JwtUtil;
import com.energyfactory.energy_factory.service.RefreshTokenService;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = "일반 회원가입을 처리하고 자동으로 로그인(JWT 발급)합니다."
    )
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto,
            HttpServletResponse response) {

        // 회원가입 처리
        SignupResponseDto signupResponse = userService.signup(signupRequestDto);

        // JWT 토큰 발급 (자동 로그인)
        String accessToken = jwtUtil.createAccessToken(
                signupResponse.getId(),
                signupResponse.getEmail(),
                "USER",
                10 * 60 * 1000L  // 10분
        );

        String refreshToken = jwtUtil.createRefreshToken(
                signupResponse.getId(),
                signupResponse.getEmail(),
                refreshTokenExpiration * 1000L  // 7일
        );

        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(signupResponse.getEmail(), refreshToken);

        // HttpOnly 쿠키에 토큰 저장
        addTokenCookie(response, "accessToken", accessToken, 10 * 60);  // 10분
        addTokenCookie(response, "refreshToken", refreshToken, refreshTokenExpiration.intValue());  // 7일

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, signupResponse));
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

    /**
     * HttpOnly 쿠키에 토큰 추가
     */
    private void addTokenCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // JavaScript 접근 방지 (XSS 방어)
        cookie.setSecure(true);    // HTTPS에서만 쿠키 전송 (프로덕션 환경)
        cookie.setPath("/");       // 모든 경로에서 접근 가능
        cookie.setMaxAge(maxAge);  // 쿠키 만료 시간 (초)
        cookie.setAttribute("SameSite", "None");  // Cross-site 쿠키 허용 (Secure=true 필요)

        response.addCookie(cookie);
    }

}