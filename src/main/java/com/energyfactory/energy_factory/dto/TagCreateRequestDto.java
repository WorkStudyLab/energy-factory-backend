package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "태그 생성 요청 DTO")
public class TagCreateRequestDto {

    @NotBlank(message = "태그명은 필수입니다")
    @Size(max = 50, message = "태그명은 50자 이하여야 합니다")
    @Schema(description = "태그명", example = "고단백", required = true)
    private String name;
}