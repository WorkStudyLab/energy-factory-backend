package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 응답 DTO")
public class SignupResponseDto {
    private Long id;
    private String email;
    private String name;
}
