package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ProductListResponseDto;
import com.energyfactory.energy_factory.dto.ProductResponseDto;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "상품 관련 API")
public class ProductController {

    @GetMapping
    @Operation(summary = "상품 목록 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductListResponseDto>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(defaultValue = "created_desc") String sort,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        ProductListResponseDto mockResponse = ProductListResponseDto.builder()
                .products(java.util.List.of())
                .pageInfo(ProductListResponseDto.PageInfoDto.builder()
                        .currentPage(0)
                        .pageSize(20)
                        .totalElements(0L)
                        .totalPages(0)
                        .first(true)
                        .last(true)
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, mockResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductResponseDto>> getProduct(
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categories/{category}")
    @Operation(summary = "카테고리별 상품 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductListResponseDto>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        ProductListResponseDto mockResponse = ProductListResponseDto.builder()
                .products(java.util.List.of())
                .pageInfo(ProductListResponseDto.PageInfoDto.builder()
                        .currentPage(0)
                        .pageSize(20)
                        .totalElements(0L)
                        .totalPages(0)
                        .first(true)
                        .last(true)
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, mockResponse));
    }

    @GetMapping("/search")
    @Operation(summary = "상품 검색")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductListResponseDto>> searchProducts(
            @RequestParam String q,
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        ProductListResponseDto mockResponse = ProductListResponseDto.builder()
                .products(java.util.List.of())
                .pageInfo(ProductListResponseDto.PageInfoDto.builder()
                        .currentPage(0)
                        .pageSize(20)
                        .totalElements(0L)
                        .totalPages(0)
                        .first(true)
                        .last(true)
                        .build())
                .build();
        
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, mockResponse));
    }
}