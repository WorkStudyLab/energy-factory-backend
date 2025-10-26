package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 코드 검증 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeResponseDto {

    @Schema(description = "비밀번호 재설정에 사용할 토큰 (10분간 유효)", example = "0cdba16d-b559-4b9b-9003-44805a9ce30c")
    private String resetToken;

    @Schema(description = "안내 메시지", example = "인증이 완료되었습니다. 10분 내에 비밀번호를 재설정해주세요.")
    private String message;
}
