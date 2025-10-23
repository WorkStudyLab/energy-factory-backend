package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "장바구니 선택 삭제 요청 DTO")
public class CartItemDeleteRequestDto {

    @NotEmpty(message = "삭제할 장바구니 아이템 ID 목록은 필수입니다")
    @Schema(description = "삭제할 장바구니 아이템 ID 목록", example = "[1, 2, 3]", required = true)
    private List<Long> cartItemIds;
}
