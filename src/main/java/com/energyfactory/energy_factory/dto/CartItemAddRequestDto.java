package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "장바구니 추가 요청 DTO")
public class CartItemAddRequestDto {

    @NotNull(message = "상품 ID는 필수입니다")
    @Positive(message = "상품 ID는 양수여야 합니다")
    @Schema(description = "상품 ID", example = "1", required = true)
    private Long productId;

    @NotNull(message = "상품 변형 ID는 필수입니다")
    @Positive(message = "상품 변형 ID는 양수여야 합니다")
    @Schema(description = "상품 변형 ID (옵션)", example = "5", required = true)
    private Long variantId;

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Max(value = 999, message = "수량은 999개 이하여야 합니다")
    @Schema(description = "수량", example = "2", required = true)
    private Integer quantity;
}
