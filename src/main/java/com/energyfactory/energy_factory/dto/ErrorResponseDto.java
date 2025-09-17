package com.energyfactory.energy_factory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "에러 응답 DTO")
public class ErrorResponseDto {

    @Schema(description = "에러 코드", example = "PRODUCT_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "상품을 찾을 수 없습니다")
    private String message;

    @Schema(description = "요청 경로", example = "/api/products/999")
    private String path;

    @Schema(description = "에러 발생 시간", example = "2024-01-01T10:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private Integer status;
}