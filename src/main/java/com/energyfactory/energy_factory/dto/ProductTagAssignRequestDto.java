package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "상품 태그 할당 요청 DTO")
public class ProductTagAssignRequestDto {

    @NotEmpty(message = "태그 ID 목록은 최소 1개 이상이어야 합니다")
    @Schema(description = "할당할 태그 ID 목록", example = "[1, 2, 3]", required = true)
    private List<@NotNull @Positive Long> tagIds;
}