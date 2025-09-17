package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tags")
@Tag(name = "Admin Tag", description = "태그 관리자 API")
public class AdminTagController {

    private final TagService tagService;

    @Autowired
    public AdminTagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    @Operation(
        summary = "전체 태그 목록 조회",
        description = "모든 태그 목록을 관리자용으로 조회합니다. 사용되지 않는 태그도 포함됩니다."
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
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<List<TagResponseDto>> getAllTags(
            @Parameter(description = "태그명 검색", example = "단백")
            @RequestParam(required = false) String keyword,
            
            @Parameter(description = "미사용 태그 포함 여부", example = "true")
            @RequestParam(defaultValue = "true") Boolean includeUnused
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok(List.of());
    }

    @PostMapping
    @Operation(
        summary = "태그 생성",
        description = "새로운 태그를 생성합니다. 중복된 태그명은 생성할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "태그 생성 성공",
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
            responseCode = "409",
            description = "중복된 태그명",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<TagResponseDto> createTag(
            @Parameter(description = "태그 생성 정보", required = true)
            @Valid @RequestBody TagCreateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "태그 상세 조회",
        description = "특정 태그의 상세 정보를 조회합니다. (관리자용)"
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

    @PutMapping("/{id}")
    @Operation(
        summary = "태그 수정",
        description = "기존 태그의 정보를 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
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
            description = "태그를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "중복된 태그명",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<TagResponseDto> updateTag(
            @Parameter(description = "태그 ID", example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "수정할 태그 정보", required = true)
            @Valid @RequestBody TagUpdateRequestDto request
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "태그 삭제",
        description = "태그를 삭제합니다. 상품에 사용 중인 태그는 삭제할 수 없습니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
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
            description = "태그를 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "삭제할 수 없음 (사용 중인 태그)",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
        )
    })
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "태그 ID", example = "1")
            @PathVariable Long id
    ) {
        // TODO: 서비스 레이어 구현 후 연동
        return ResponseEntity.noContent().build();
    }
}