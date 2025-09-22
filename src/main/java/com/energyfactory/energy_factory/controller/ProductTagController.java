package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.ProductTagService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품-태그 연관 관리 컨트롤러
 * 상품과 태그 간의 연결 관리 및 태그 기반 상품 조회 기능을 제공
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Tag", description = "상품-태그 관리 API")
@RequiredArgsConstructor
public class ProductTagController {

    private final ProductTagService productTagService;

    @GetMapping("/{productId}/tags")
    @Operation(summary = "상품의 태그 목록 조회")
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> getProductTags(
            @PathVariable Long productId
    ) {
        List<TagResponseDto> tags = productTagService.getProductTags(productId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, tags));
    }

    @PostMapping("/{productId}/tags/{tagId}")
    @Operation(summary = "상품에 태그 추가")
    public ResponseEntity<ApiResponse<Void>> addTagToProduct(
            @PathVariable Long productId,
            @PathVariable Long tagId
    ) {
        productTagService.addTagToProduct(productId, tagId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, null));
    }

    @PostMapping("/{productId}/tags")
    @Operation(summary = "상품에 여러 태그 추가")
    public ResponseEntity<ApiResponse<Void>> addTagsToProduct(
            @PathVariable Long productId,
            @RequestBody ProductTagBulkRequestDto requestDto
    ) {
        productTagService.addTagsToProduct(productId, requestDto.getTagIds());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, null));
    }

    @DeleteMapping("/{productId}/tags/{tagId}")
    @Operation(summary = "상품에서 태그 제거")
    public ResponseEntity<ApiResponse<Void>> removeTagFromProduct(
            @PathVariable Long productId,
            @PathVariable Long tagId
    ) {
        productTagService.removeTagFromProduct(productId, tagId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @DeleteMapping("/{productId}/tags")
    @Operation(summary = "상품의 모든 태그 제거")
    public ResponseEntity<ApiResponse<Void>> removeAllTagsFromProduct(
            @PathVariable Long productId
    ) {
        productTagService.removeAllTagsFromProduct(productId);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }

    @GetMapping("/by-tag/{tagId}")
    @Operation(summary = "특정 태그가 적용된 상품 목록 조회")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getProductsByTag(
            @PathVariable Long tagId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductListResponseDto response = productTagService.getProductsByTag(tagId, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @GetMapping("/by-tags/all")
    @Operation(summary = "모든 태그가 적용된 상품 목록 조회 (AND 조건)")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getProductsByAllTags(
            @RequestParam List<Long> tagIds,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductListResponseDto response = productTagService.getProductsByAllTags(tagIds, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @GetMapping("/by-tags/any")
    @Operation(summary = "일부 태그가 적용된 상품 목록 조회 (OR 조건)")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getProductsByAnyTags(
            @RequestParam List<Long> tagIds,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductListResponseDto response = productTagService.getProductsByAnyTags(tagIds, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }
}