package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "장바구니 수량 변경 요청 DTO")
public class CartItemUpdateRequestDto {

    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    @Max(value = 999, message = "수량은 999개 이하여야 합니다")
    @Schema(description = "변경할 수량", example = "5", required = true)
    private Integer quantity;
}
