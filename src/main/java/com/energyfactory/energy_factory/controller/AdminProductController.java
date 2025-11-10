package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.AdminProductService;
import com.energyfactory.energy_factory.service.ProductService;
import com.energyfactory.energy_factory.service.S3Service;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 상품 관리 컨트롤러
 * ADMIN 권한이 필요한 상품 CRUD 기능 제공
 */
@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Admin Product", description = "관리자 상품 관리 API (ADMIN 권한 필요)")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;
    private final ProductService productService;

    @Autowired(required = false)
    private S3Service s3Service;

    /**
     * 상품 목록 조회 (관리자용 - 기존 ProductService 재사용)
     */
    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "페이지네이션, 필터링, 검색을 지원하는 상품 목록 조회")
    public ResponseEntity<ApiResponse<ProductListResponseDto>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        ProductListResponseDto response = productService.getProducts(
                category, keyword, status, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    /**
     * 상품 단일 조회 (관리자용 - 기존 ProductService 재사용)
     */
    @GetMapping("/{id}")
    @Operation(summary = "상품 상세 조회", description = "특정 상품의 상세 정보 조회")
    public ResponseEntity<ApiResponse<ProductResponseDto>> getProduct(@PathVariable Long id) {
        try {
            ProductResponseDto response = productService.getProductById(id);
            return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ResultCode.NOT_FOUND, null));
        }
    }

    /**
     * 상품 생성
     */
    @PostMapping
    @Operation(summary = "상품 생성", description = "새로운 상품 생성 (태그 포함)")
    public ResponseEntity<ApiResponse<ProductResponseDto>> createProduct(
            @Valid @RequestBody ProductCreateRequestDto requestDto
    ) {
        try {
            ProductResponseDto response = adminProductService.createProduct(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.of(ResultCode.SUCCESS_POST, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ResultCode.INVALID_REQUEST, null));
        }
    }

    /**
     * 상품 수정
     */
    @PutMapping("/{id}")
    @Operation(summary = "상품 수정", description = "기존 상품 정보 수정 (태그 포함)")
    public ResponseEntity<ApiResponse<ProductResponseDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequestDto requestDto
    ) {
        try {
            ProductResponseDto response = adminProductService.updateProduct(id, requestDto);
            return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.of(ResultCode.NOT_FOUND, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ResultCode.INVALID_REQUEST, null));
        }
    }

    /**
     * 상품 삭제
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "상품 삭제", description = "상품 삭제 (주문 내역이 있으면 삭제 불가)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        try {
            adminProductService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("주문 내역")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.of(ResultCode.INVALID_REQUEST, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.of(ResultCode.NOT_FOUND, null));
            }
        }
    }

    /**
     * 카테고리 목록 조회
     */
    @GetMapping("/categories")
    @Operation(summary = "카테고리 목록 조회", description = "DB에 존재하는 모든 카테고리 목록 조회")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> getCategories() {
        List<String> categories = productService.getAllCategories();
        Map<String, List<String>> response = new HashMap<>();
        response.put("categories", categories);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    /**
     * 상품 이미지 업로드
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "상품 이미지 업로드", description = "상품 이미지 파일을 S3에 업로드하고 URL 반환")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file
    ) {
        // S3Service가 로드되지 않은 경우 (로컬 환경)
        if (s3Service == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.of(ResultCode.INTERNAL_SERVER_ERROR, null));
        }

        try {
            // S3에 파일 업로드 (products 디렉토리에 저장)
            String imageUrl = s3Service.uploadImage(file, "products");

            // 응답 데이터 구성
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));

        } catch (IllegalArgumentException e) {
            // 파일 검증 실패
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.of(ResultCode.INVALID_REQUEST, null));
        } catch (Exception e) {
            // 업로드 실패
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ResultCode.INTERNAL_SERVER_ERROR, null));
        }
    }

    /**
     * 상품 이미지 삭제
     */
    @DeleteMapping("/images")
    @Operation(summary = "상품 이미지 삭제", description = "S3에 업로드된 상품 이미지 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @RequestParam("imageUrl") String imageUrl
    ) {
        // S3Service가 로드되지 않은 경우 (로컬 환경)
        if (s3Service == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.of(ResultCode.INTERNAL_SERVER_ERROR, null));
        }

        try {
            s3Service.deleteImage(imageUrl);
            return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.of(ResultCode.INTERNAL_SERVER_ERROR, null));
        }
    }
}
