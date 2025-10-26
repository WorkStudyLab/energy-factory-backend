package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDto {

    @Schema(description = "인증 완료 후 발급받은 리셋 토큰", example = "0cdba16d-b559-4b9b-9003-44805a9ce30c")
    @NotBlank(message = "리셋 토큰은 필수입니다")
    private String resetToken;

    @Schema(description = "새 비밀번호 (8자 이상, 대소문자/숫자/특수문자 포함)", example = "NewPass123!")
    @NotBlank(message = "새 비밀번호는 필수입니다")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "비밀번호는 최소 8자 이상이며, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다"
    )
    private String newPassword;
}
