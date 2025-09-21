package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.TagResponseDto;
import com.energyfactory.energy_factory.service.TagService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tag", description = "태그 관련 API")
public class TagController {

    private final TagService tagService;

    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(summary = "태그 목록 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<List<TagResponseDto>>> getAllTags(
            @RequestParam(required = false) String keyword
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, List.of()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "태그 상세 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<TagResponseDto>> getTag(
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 태그 조회")
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<List<TagResponseDto>>> getPopularTags(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, List.of()));
    }
}