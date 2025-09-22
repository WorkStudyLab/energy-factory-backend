package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ApiResponse;
import com.energyfactory.energy_factory.dto.TagCreateRequestDto;
import com.energyfactory.energy_factory.dto.TagListResponseDto;
import com.energyfactory.energy_factory.dto.TagResponseDto;
import com.energyfactory.energy_factory.dto.TagUpdateRequestDto;
import com.energyfactory.energy_factory.service.TagService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 태그 관리 컨트롤러
 * 태그 생성, 조회, 수정, 삭제, 검색 기능을 제공
 */
@RestController
@RequestMapping("/api/tags")
@Tag(name = "Tag Management", description = "태그 관리 관련 API")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    @Operation(summary = "태그 생성")
    public ResponseEntity<ApiResponse<TagResponseDto>> createTag(
            @Valid @RequestBody TagCreateRequestDto createRequestDto) {
        TagResponseDto createdTag = tagService.createTag(createRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.of(ResultCode.SUCCESS_POST, createdTag));
    }

    @GetMapping
    @Operation(summary = "태그 목록 조회")
    public ResponseEntity<ApiResponse<TagListResponseDto>> getAllTags(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        TagListResponseDto response;
        if (keyword != null && !keyword.trim().isEmpty()) {
            response = tagService.searchTags(keyword, pageable);
        } else {
            response = tagService.getAllTags(pageable);
        }
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "태그 상세 조회")
    public ResponseEntity<ApiResponse<TagResponseDto>> getTag(@PathVariable Long id) {
        TagResponseDto tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, tag));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "태그명으로 태그 조회")
    public ResponseEntity<ApiResponse<TagResponseDto>> getTagByName(@PathVariable String name) {
        TagResponseDto tag = tagService.getTagByName(name);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, tag));
    }

    @GetMapping("/popular")
    @Operation(summary = "인기 태그 조회")
    public ResponseEntity<ApiResponse<TagListResponseDto>> getPopularTags(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        TagListResponseDto response = tagService.getPopularTags(pageable);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "태그 수정")
    public ResponseEntity<ApiResponse<TagResponseDto>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagUpdateRequestDto updateRequestDto) {
        TagResponseDto updatedTag = tagService.updateTag(id, updateRequestDto);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, updatedTag));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "태그 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.of(ResultCode.SUCCESS, null));
    }
}