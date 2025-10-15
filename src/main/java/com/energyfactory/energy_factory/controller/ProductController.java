package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ProductListResponseDto;
import com.energyfactory.energy_factory.dto.ProductResponseDto;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.service.ProductService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 상품 관리 컨트롤러
 * 상품 목록 조회, 상세 조회, 카테고리별 조회, 검색 기능을 제공
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "상품 관련 API")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "상품 목록 조회")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductListResponseDto response = productService.getProducts(
                category, keyword, status, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(
            @PathVariable Long id
    ) {
        try {
            ProductResponseDto response = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categories/{category}")
    @Operation(summary = "카테고리별 상품 조회")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductListResponseDto response = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @GetMapping("/search")
    @Operation(summary = "상품 검색")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> searchProducts(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductListResponseDto response = productService.searchProducts(q, category, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @GetMapping("/unified-search")
    @Operation(summary = "통합 검색 (상품명, 태그명, 영양소명, 설명)")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> unifiedSearch(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ProductListResponseDto response = productService.unifiedSearch(q, category, pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }
}