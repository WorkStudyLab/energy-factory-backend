package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "태그 응답 DTO")
public class TagResponseDto {

    @Schema(description = "태그 ID", example = "1")
    private Long id;

    @Schema(description = "태그명", example = "고단백")
    private String name;

    @Schema(description = "이 태그가 적용된 상품 수", example = "15")
    private Long productCount;

}