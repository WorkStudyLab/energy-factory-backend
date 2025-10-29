package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.EmailService;
import com.energyfactory.energy_factory.service.UserService;
import com.energyfactory.energy_factory.service.VerificationCodeService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 비밀번호 재설정 컨트롤러
 * 비밀번호 찾기 기능을 제공
 */
@RestController
@RequestMapping("/api/auth/password-reset")
@Tag(name = "Password Reset", description = "비밀번호 재설정 관련 API")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.mail.host")
public class PasswordResetController {

    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
    private final UserService userService;

    @PostMapping("/send-code")
    @Operation(
        summary = "인증 코드 전송",
        description = "이메일로 6자리 인증 코드를 전송합니다. 코드는 5분간 유효하며, 1분에 1회만 요청 가능합니다."
    )
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody SendCodeRequestDto request
    ) {
        // 인증 코드 생성
        String code = verificationCodeService.generateCode();

        // Redis에 저장
        verificationCodeService.saveCode(request.getEmail(), code);

        // 이메일 발송
        emailService.sendVerificationCode(request.getEmail(), code);

        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @PostMapping("/verify-code")
    @Operation(
        summary = "인증 코드 검증",
        description = "사용자가 입력한 6자리 코드를 검증하고, 검증 성공 시 비밀번호 재설정용 임시 토큰을 발급합니다."
    )
    public ResponseEntity<ApiResponse<VerifyCodeResponseDto>> verifyCode(
            @Valid @RequestBody VerifyCodeRequestDto request
    ) {
        // 인증 코드 검증
        verificationCodeService.verifyCode(request.getEmail(), request.getCode());

        // 리셋 토큰 생성
        String resetToken = verificationCodeService.generateResetToken(request.getEmail());

        VerifyCodeResponseDto response = VerifyCodeResponseDto.builder()
                .resetToken(resetToken)
                .message("인증이 완료되었습니다. 10분 내에 비밀번호를 재설정해주세요.")
                .build();

        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @PostMapping
    @Operation(
        summary = "비밀번호 재설정",
        description = "인증 완료 후 발급받은 토큰으로 비밀번호를 재설정합니다. 토큰은 10분간 유효합니다."
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto request
    ) {
        // 리셋 토큰 검증 및 이메일 조회
        String email = verificationCodeService.validateResetToken(request.getResetToken());

        // 비밀번호 재설정
        userService.resetPasswordByEmail(email, request.getNewPassword());

        // 토큰 삭제 (재사용 방지)
        verificationCodeService.deleteResetToken(request.getResetToken());

        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }
}
