package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.ProductTagService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Tag", description = "상품-태그 관리 API")
public class ProductTagController {

    private final ProductTagService productTagService;

    @Autowired
    public ProductTagController(ProductTagService productTagService) {
        this.productTagService = productTagService;
    }

    @GetMapping("/{productId}/tags")
    @Operation(summary = "상품의 태그 목록 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<List<TagResponseDto>>> getProductTags(
            @PathVariable Long productId
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, List.of()));
    }
}