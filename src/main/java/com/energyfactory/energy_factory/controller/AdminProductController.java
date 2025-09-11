package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ProductCreateRequestDto;
import com.energyfactory.energy_factory.dto.ProductResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Admin Product", description = "상품 관리자 API")
public class AdminProductController {

    @PostMapping
    @Operation(
        summary = "상품 등록",
        description = "새로운 상품을 등록합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "상품 등록 성공",
            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ProductResponseDto> createProduct(
            @Parameter(description = "상품 생성 정보", required = true)
            @Valid @RequestBody ProductCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "상품 수정",
        description = "기존 상품 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "상품 수정 성공",
            content = @Content(schema = @Schema(implementation = ProductResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ProductResponseDto> updateProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "수정할 상품 정보", required = true)
            @Valid @RequestBody ProductCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "상품 삭제",
        description = "상품을 삭제합니다. (소프트 삭제 권장)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "상품 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        @ApiResponse(responseCode = "409", description = "삭제할 수 없음 (주문 내역 존재)"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/stock")
    @Operation(
        summary = "재고 수량 업데이트",
        description = "상품의 재고 수량을 업데이트합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "재고 업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 재고 수량"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Void> updateStock(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "새로운 재고 수량", example = "50")
            @RequestParam Long stock
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "상품 상태 변경",
        description = "상품의 판매 상태를 변경합니다. (AVAILABLE, SOLDOUT, DISCONTINUED)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 상태 값"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "새로운 상품 상태", example = "AVAILABLE")
            @RequestParam String status
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }
}