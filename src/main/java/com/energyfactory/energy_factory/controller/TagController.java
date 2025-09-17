package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.ErrorResponseDto;
import com.energyfactory.energy_factory.dto.TagResponseDto;
import com.energyfactory.energy_factory.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "태그 목록 조회",
        description = "등록된 모든 태그 목록을 조회합니다. 각 태그별 상품 수도 함께 제공됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<List<TagResponseDto>> getAllTags(
            @Parameter(description = "태그명 검색", example = "단백")
            @RequestParam(required = false) String keyword
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "태그 상세 조회",
        description = "특정 태그의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "태그를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<TagResponseDto> getTag(
            @Parameter(description = "태그 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/popular")
    @Operation(
        summary = "인기 태그 조회",
        description = "가장 많은 상품에 사용된 태그들을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = TagResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<List<TagResponseDto>> getPopularTags(
            @Parameter(description = "조회할 태그 수", example = "10")
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(List.of());
    }
}