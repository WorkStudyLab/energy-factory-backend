package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "사용자 정보 수정 요청 DTO")
public class UserUpdateRequestDto {

    @NotBlank(message = "이메일은 필수입니다")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "이메일 형식이 올바르지 않습니다")
    @Schema(description = "이메일", example = "user@example.com", required = true)
    private String email;

    @NotBlank(message = "이름은 필수입니다")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다")
    @Schema(description = "이름", example = "홍길동", required = true)
    private String name;

    @NotBlank(message = "전화번호는 필수입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (010-XXXX-XXXX)")
    @Schema(description = "전화번호", example = "010-1234-5678", required = true)
    private String phoneNumber;
}