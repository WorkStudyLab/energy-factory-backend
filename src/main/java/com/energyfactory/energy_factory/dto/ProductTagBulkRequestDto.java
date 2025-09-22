package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 태그 일괄 추가 요청 DTO")
public class ProductTagBulkRequestDto {

    @NotEmpty(message = "태그 ID 목록은 필수입니다")
    @Schema(description = "태그 ID 목록", example = "[1, 2, 3]", required = true)
    private List<Long> tagIds;
}