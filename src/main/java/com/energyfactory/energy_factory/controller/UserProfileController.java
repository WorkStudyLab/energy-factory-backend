package com.energyfactory.energy_factory.controller;

import com.energyfactory.energy_factory.dto.*;
import com.energyfactory.energy_factory.service.UserProfileService;
import com.energyfactory.energy_factory.utils.enums.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 프로필 관리 컨트롤러
 * 사용자 프로필 기본 CRUD 기능을 제공
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "사용자 프로필 관련 API")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 생성", description = "새로운 사용자 프로필을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "이미 프로필이 존재함", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> createProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserProfileCreateRequestDto requestDto
    ) {
        UserProfileResponseDto profile = userProfileService.createProfile(userId, requestDto);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 조회", description = "사용자 ID로 프로필을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> getProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId
    ) {
        UserProfileResponseDto profile = userProfileService.getProfileByUserId(userId);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @PutMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 수정", description = "사용자 프로필 정보를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<UserProfileResponseDto>> updateProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId,
            @Valid @RequestBody UserProfileUpdateRequestDto requestDto
    ) {
        UserProfileResponseDto profile = userProfileService.updateProfile(userId, requestDto);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, profile));
    }

    @DeleteMapping("/{userId}/profile")
    @Operation(summary = "사용자 프로필 삭제", description = "사용자 프로필을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "프로필을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    public ResponseEntity<com.energyfactory.energy_factory.dto.ApiResponse<Void>> deleteProfile(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId
    ) {
        userProfileService.deleteProfile(userId);
        return ResponseEntity.ok(com.energyfactory.energy_factory.dto.ApiResponse.of(ResultCode.SUCCESS, null));
    }
}