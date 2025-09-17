package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.ProductTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Product Tag", description = "상품-태그 관리 API")
public class ProductTagController {

    private final ProductTagService productTagService;

    @Autowired
    public ProductTagController(ProductTagService productTagService) {
        this.productTagService = productTagService;
    }

    @GetMapping("/{productId}/tags")
    @Operation(
        summary = "상품의 태그 목록 조회",
        description = "특정 상품에 할당된 모든 태그를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<List<TagResponseDto>> getProductTags(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/{productId}/tags")
    @Operation(
        summary = "상품에 태그 할당",
        description = "상품에 여러 태그를 할당합니다. 기존 태그는 모두 해제되고 새로운 태그로 교체됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "할당 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품 또는 태그를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<List<TagResponseDto>> assignTagsToProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            
            @Parameter(description = "할당할 태그 정보", required = true)
            @Valid @RequestBody ProductTagAssignRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(List.of());
    }

    @PutMapping("/{productId}/tags/{tagId}")
    @Operation(
        summary = "상품에 개별 태그 추가",
        description = "상품에 특정 태그를 추가합니다. 기존 태그는 유지됩니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "추가 성공"),
        @ApiResponse(
            responseCode = "400",
            description = "이미 할당된 태그",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품 또는 태그를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<Void> addTagToProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            
            @Parameter(description = "태그 ID", example = "2")
            @PathVariable Long tagId
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}/tags/{tagId}")
    @Operation(
        summary = "상품에서 태그 제거",
        description = "상품에서 특정 태그를 제거합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "제거 성공"),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품, 태그 또는 할당 관계를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<Void> removeTagFromProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId,
            
            @Parameter(description = "태그 ID", example = "2")
            @PathVariable Long tagId
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/tags")
    @Operation(
        summary = "상품의 모든 태그 제거",
        description = "상품에 할당된 모든 태그를 제거합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "제거 성공"),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "상품을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<Void> removeAllTagsFromProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long productId
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.noContent().build();
    }
}