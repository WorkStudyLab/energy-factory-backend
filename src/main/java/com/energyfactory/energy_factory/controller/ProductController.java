package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ProductListResponseDto;
import com.energyfactory.energy_factory.dto.ProductResponseDto;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "상품 목록 조회",
        description = "페이징과 필터링이 가능한 상품 목록을 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = com.energyfactory.energy_factory.dto.ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductListResponseDto>> getProducts(
            @Parameter(description = "카테고리 필터 (예: 고기, 채소, 생선)", example = "고기")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "검색 키워드 (상품명, 브랜드명)", example = "한우")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "판매 상태 필터", example = "AVAILABLE")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "최소 가격", example = "10000")
            @RequestParam(required = false) Integer minPrice,
            
            @Parameter(description = "최대 가격", example = "50000")
            @RequestParam(required = false) Integer maxPrice,
            
            @Parameter(description = "정렬 기준 (price_asc, price_desc, name_asc, created_desc)", example = "created_desc")
            @RequestParam(defaultValue = "created_desc") String sort,
            
            @Parameter(description = "페이징 정보")
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
    @Operation(
        summary = "상품 상세 조회",
        description = "특정 상품의 상세 정보를 조회합니다. 태그와 영양정보가 포함됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = com.energyfactory.energy_factory.dto.ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "상품을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductResponseDto>> getProduct(
            @Parameter(description = "상품 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categories/{category}")
    @Operation(
        summary = "카테고리별 상품 조회",
        description = "특정 카테고리의 상품 목록을 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = com.energyfactory.energy_factory.dto.ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 카테고리",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductListResponseDto>> getProductsByCategory(
            @Parameter(description = "카테고리명 (고기, 채소, 생선 등)", example = "고기")
            @PathVariable String category,
            
            @Parameter(description = "페이징 정보")
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
    @Operation(
        summary = "상품 검색",
        description = "키워드로 상품을 검색합니다. 상품명, 브랜드명, 설명에서 검색됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @Content(schema = @Schema(implementation = com.energyfactory.energy_factory.dto.ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 검색어",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<ProductListResponseDto>> searchProducts(
            @Parameter(description = "검색 키워드", example = "한우", required = true)
            @RequestParam String q,
            
            @Parameter(description = "카테고리 필터", example = "고기")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "페이징 정보")
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